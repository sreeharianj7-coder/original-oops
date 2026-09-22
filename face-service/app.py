"""
==============================================================================
SMART HOSTEL ATTENDANCE — PYTHON FACIAL RECOGNITION REST API
==============================================================================

Endpoints:
  GET  /health                   - Service & model health diagnostics
  POST /face/enroll              - Processes & embeds up to 10 enrollment photos
  POST /face/verify              - Verifies captured live face against enrolled embeddings
  POST /face/config/threshold    - Updates matching threshold dynamically
==============================================================================
"""

import os
import logging
from flask import Flask, request, jsonify
from flask_cors import CORS
from face_engine import FaceEngine
from embedding_service import EmbeddingService

# Configure Logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("FaceAPI")

app = Flask(__name__)
CORS(app)  # Enable Cross-Origin requests

PORT = int(os.getenv("PORT", "5000"))
MAX_PHOTOS_PER_STUDENT = 10


@app.route("/health", methods=["GET"])
@app.route("/face/status", methods=["GET"])
def health_check():
    """Returns service health, pretrained model status, and current threshold."""
    return jsonify({
        "status": "UP",
        "service": "FaceNet Facial Biometric Service",
        "model": "InceptionResnetV1 (Pretrained on VGGFace2)",
        "detector": "MTCNN (Multi-task Cascaded Convolutional Networks)",
        "threshold": EmbeddingService.get_threshold(),
        "maxPhotosPerStudent": MAX_PHOTOS_PER_STUDENT,
        "cv2Used": False
    }), 200


@app.route("/face/enroll", methods=["POST"])
def enroll_faces():
    """
    Enrolls a student's facial profile using up to 10 photographs.
    Processes each image independently through MTCNN face alignment and FaceNet.
    Enforces the single-face rule per photo.

    Expected JSON body:
    {
        "studentId": "ASIET2024CS001",
        "images": ["data:image/jpeg;base64,...", "data:image/jpeg;base64,..."]
    }
    """
    try:
        data = request.get_json(force=True) if request.is_json else request.form.to_dict()
    except Exception:
        return jsonify({"success": False, "message": "Invalid JSON request body"}), 400

    student_id = data.get("studentId")
    if not student_id or not str(student_id).strip():
        return jsonify({"success": False, "message": "Missing required field: studentId"}), 400

    student_id = str(student_id).strip().upper()
    images = data.get("images", [])

    if not isinstance(images, list) or len(images) == 0:
        return jsonify({"success": False, "message": "At least 1 face photo is required for enrollment"}), 400

    if len(images) > MAX_PHOTOS_PER_STUDENT:
        return jsonify({
            "success": False,
            "message": f"Maximum of {MAX_PHOTOS_PER_STUDENT} photos allowed per student. Received: {len(images)}"
        }), 400

    logger.info(f"Processing enrollment for Student ID: {student_id} with {len(images)} photos...")

    results = []
    valid_count = 0
    valid_embeddings = []

    for idx, img_data in enumerate(images):
        photo_index = idx + 1
        res = FaceEngine.process_and_extract_embedding(img_data)

        if res["success"] and res["embedding"] is not None:
            valid_count += 1
            valid_embeddings.append({
                "photoIndex": photo_index,
                "embedding": res["embedding"]
            })
            results.append({
                "photoIndex": photo_index,
                "valid": True,
                "embedding": res["embedding"],
                "message": "Face embedding extracted successfully"
            })
        else:
            results.append({
                "photoIndex": photo_index,
                "valid": False,
                "embedding": None,
                "message": res.get("error", "Unable to process this image.")
            })

    all_valid = (valid_count == len(images))

    # Requirement 6: Verify all uploaded registration photos belong to the same student
    if valid_count > 1:
        raw_embeddings = [item["embedding"] for item in valid_embeddings]
        consistency = EmbeddingService.verify_same_person_consistency(raw_embeddings)
        if not consistency["isConsistent"]:
            logger.warning(f"Registration rejected for {student_id}: {consistency['message']}")
            return jsonify({
                "success": False,
                "studentId": student_id,
                "totalSubmitted": len(images),
                "validCount": 0,
                "registeredPhotos": 0,
                "allPhotosValid": False,
                "message": consistency["message"],
                "results": results,
                "embeddings": []
            }), 400

    logger.info(
        f"Enrollment processed for {student_id}: {valid_count}/{len(images)} valid photos. "
        f"All Valid: {all_valid}"
    )

    return jsonify({
        "success": valid_count > 0,
        "studentId": student_id,
        "totalSubmitted": len(images),
        "validCount": valid_count,
        "registeredPhotos": valid_count,
        "allPhotosValid": all_valid,
        "results": results,
        "embeddings": valid_embeddings
    }), 200



@app.route("/face/verify", methods=["POST"])
def verify_face():
    """
    Verifies a live captured student photo against their registered templates.

    Expected JSON body:
    {
        "studentId": "ASIET2024CS001",
        "liveImage": "data:image/jpeg;base64,...",
        "registeredEmbeddings": [[...512 floats...], ...] (optional if provided by Java backend)
    }
    """
    try:
        data = request.get_json(force=True) if request.is_json else request.form.to_dict()
    except Exception:
        return jsonify({"success": False, "matched": False, "message": "Invalid JSON request body"}), 400

    student_id = data.get("studentId", "UNKNOWN").strip().upper()
    live_image = data.get("liveImage")
    registered_embeddings = data.get("registeredEmbeddings", [])

    if not live_image:
        return jsonify({
            "success": False,
            "matched": False,
            "message": "Missing live image payload for verification."
        }), 400

    logger.info(f"Running live face verification for Student ID: {student_id}...")

    # Step 1: Process live captured image & extract embedding
    live_result = FaceEngine.process_and_extract_embedding(live_image)

    if not live_result["success"] or live_result["embedding"] is None:
        error_msg = live_result.get("error", "Face verification failed. Could not process live camera frame.")
        logger.warning(f"Live face extraction failed for {student_id}: {error_msg}")
        return jsonify({
            "success": False,
            "matched": False,
            "similarity": 0.0,
            "threshold": EmbeddingService.get_threshold(),
            "message": error_msg,
            "reason": error_msg
        }), 200

    live_embedding = live_result["embedding"]

    # Step 2: Compare against registered embeddings
    if not registered_embeddings or len(registered_embeddings) == 0:
        return jsonify({
            "success": False,
            "matched": False,
            "similarity": 0.0,
            "threshold": EmbeddingService.get_threshold(),
            "message": "Student does not have registered face profiles. Please complete face registration first.",
            "reason": "Not registered"
        }), 200

    comparison = EmbeddingService.compare_live_against_enrolled(live_embedding, registered_embeddings)

    return jsonify({
        "success": True,
        "studentId": student_id,
        "matched": comparison["matched"],
        "similarity": comparison["maxSimilarity"],
        "threshold": comparison["threshold"],
        "bestMatchIndex": comparison["bestMatchIndex"],
        "message": comparison["message"],
        "details": comparison["photoScores"]
    }), 200


@app.route("/face/config/threshold", methods=["POST"])
def update_threshold():
    """Allows dynamic adjustment of the verification threshold during testing."""
    data = request.get_json(force=True) or {}
    new_threshold = data.get("threshold")
    if new_threshold is None:
        return jsonify({"error": "Missing 'threshold' field"}), 400

    try:
        val = float(new_threshold)
        if val <= 0.0 or val >= 1.0:
            return jsonify({"error": "Threshold must be between 0.0 and 1.0"}), 400
        EmbeddingService.set_threshold(val)
        return jsonify({"success": True, "threshold": EmbeddingService.get_threshold()}), 200
    except ValueError:
        return jsonify({"error": "Invalid floating point value for threshold"}), 400


if __name__ == "__main__":
    logger.info(f"Starting Smart Hostel Face Recognition Microservice on port {PORT}...")
    app.run(host="0.0.0.0", port=PORT, debug=False)

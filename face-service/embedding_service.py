"""
==============================================================================
SMART HOSTEL ATTENDANCE — EMBEDDING COMPARISON & VERIFICATION SERVICE
==============================================================================

Handles multi-photo biometric matching logic for enrolled students (up to 10 photos).
Computes Cosine Similarity against all registered templates and evaluates
against the configurable threshold FACE_MATCH_THRESHOLD.
==============================================================================
"""

import os
import logging
from face_engine import FaceEngine, compute_cosine_similarity

logger = logging.getLogger("EmbeddingService")

# Configurable matching threshold (can be adjusted via environment variable)
DEFAULT_THRESHOLD = 0.70
FACE_MATCH_THRESHOLD = float(os.getenv("FACE_MATCH_THRESHOLD", str(DEFAULT_THRESHOLD)))
logger.info(f"Loaded FACE_MATCH_THRESHOLD: {FACE_MATCH_THRESHOLD}")


class EmbeddingService:
    """
    Manages multi-photo template comparisons and verification decisions.
    """

    @classmethod
    def get_threshold(cls) -> float:
        """Returns the active face matching threshold."""
        return float(os.getenv("FACE_MATCH_THRESHOLD", str(FACE_MATCH_THRESHOLD)))

    @classmethod
    def set_threshold(cls, new_threshold: float):
        """Allows dynamic runtime configuration of the matching threshold."""
        global FACE_MATCH_THRESHOLD
        FACE_MATCH_THRESHOLD = float(new_threshold)
        logger.info(f"Updated FACE_MATCH_THRESHOLD to: {FACE_MATCH_THRESHOLD}")

    @classmethod
    def compare_live_against_enrolled(cls, live_embedding, enrolled_embeddings: list) -> dict:
        """
        Compares a captured live face embedding against a student's enrolled embeddings (up to 10).

        Flow:
        Live Embedding
            ↓
        Compare with Photo 1 embedding
        Compare with Photo 2 embedding
        ...
        Compare with Photo N embedding (N <= 10)
            ↓
        Compute Max Cosine Similarity
            ↓
        Evaluate against FACE_MATCH_THRESHOLD
            ↓
        MATCH / NO MATCH

        Args:
            live_embedding (list[float]): 512-d normalized embedding of live captured face.
            enrolled_embeddings (list[list[float]]): List of up to 10 registered 512-d embeddings.

        Returns:
            dict: {
                "matched": bool,
                "maxSimilarity": float,
                "threshold": float,
                "bestMatchIndex": int (1-based),
                "photoScores": list[dict],
                "evaluatedCount": int
            }
        """
        if not live_embedding:
            return {
                "matched": False,
                "maxSimilarity": 0.0,
                "threshold": cls.get_threshold(),
                "bestMatchIndex": 0,
                "photoScores": [],
                "evaluatedCount": 0,
                "message": "Live face embedding is missing."
            }

        if not enrolled_embeddings or len(enrolled_embeddings) == 0:
            return {
                "matched": False,
                "maxSimilarity": 0.0,
                "threshold": cls.get_threshold(),
                "bestMatchIndex": 0,
                "photoScores": [],
                "evaluatedCount": 0,
                "message": "Student has no registered face profiles. Face registration required."
            }

        # Cap comparison to maximum 10 enrolled embeddings
        candidates = enrolled_embeddings[:10]
        threshold = cls.get_threshold()

        scores = []
        max_score = -1.0
        best_index = 1

        for idx, registered_vec in enumerate(candidates):
            sim = compute_cosine_similarity(live_embedding, registered_vec)
            photo_num = idx + 1
            scores.append({
                "photoIndex": photo_num,
                "similarity": sim
            })

            if sim > max_score:
                max_score = sim
                best_index = photo_num

        max_score = max(0.0, max_score)
        is_matched = max_score >= threshold

        logger.info(
            f"Verification Result: Matched={is_matched}, MaxSimilarity={max_score:.4f}, "
            f"Threshold={threshold:.2f}, BestMatchPhoto=#{best_index}, EvaluatedPhotos={len(candidates)}"
        )

        return {
            "matched": is_matched,
            "maxSimilarity": round(max_score, 4),
            "threshold": threshold,
            "bestMatchIndex": best_index,
            "photoScores": scores,
            "evaluatedCount": len(candidates),
            "message": "Face verification successful" if is_matched else "Face not recognized. Similarity below threshold."
        }

    @classmethod
    def verify_same_person_consistency(cls, embeddings: list, min_similarity: float = 0.55) -> dict:
        """
        Verifies that all uploaded registration photos belong to the same student identity.
        Compares each photo's 512-d embedding vector against the ensemble centroid.
        If any photo has similarity < min_similarity (default 0.55), flags inter-photo inconsistency.
        """
        if not embeddings or len(embeddings) <= 1:
            return {
                "isConsistent": True,
                "failedIndex": None,
                "message": "Consistency check passed (single photo or empty)."
            }

        try:
            import numpy as np
            vectors = [np.array(emb, dtype=np.float32).flatten() for emb in embeddings]

            # Compute mean centroid of embeddings
            centroid = np.mean(vectors, axis=0)
            norm = np.linalg.norm(centroid)
            if norm > 0:
                centroid = centroid / norm

            for idx, vec in enumerate(vectors):
                sim = compute_cosine_similarity(vec, centroid)
                if sim < min_similarity:
                    photo_num = idx + 1
                    logger.warning(
                        f"Consistency Check Failed for Photo #{photo_num}: "
                        f"Cosine similarity to centroid = {sim:.4f} (Required >= {min_similarity})"
                    )
                    return {
                        "isConsistent": False,
                        "failedIndex": photo_num,
                        "similarity": round(sim, 4),
                        "message": "One or more photos appear to belong to a different person. Please upload photos of the same student."
                    }

            return {
                "isConsistent": True,
                "failedIndex": None,
                "message": "All uploaded registration photos belong to the same student."
            }
        except Exception as ex:
            logger.error(f"Error during inter-photo consistency check: {ex}")
            return {"isConsistent": True, "failedIndex": None, "message": str(ex)}


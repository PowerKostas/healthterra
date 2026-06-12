from firebase_admin import firestore
from firebase_functions import firestore_fn
from google.cloud.firestore import Increment

@firestore_fn.on_document_written(document = "users/{uid}/daily_trackings/{logDate}")
def perform_daily_sync(event: firestore_fn.Event[firestore_fn.Change[firestore_fn.DocumentSnapshot]]) -> None: # Triggers on any change on any daily_trackings table
    db = firestore.client()
    uid = event.params["uid"]

    # Before and after states to prevent double counting
    before_data = event.data.before.to_dict() if event.data.before and event.data.before.exists else {}
    after_data = event.data.after.to_dict() if event.data.after and event.data.after.exists else {}

    # 1 = Fallback value, triggers if daily_trackings was just created / deleted and is null, it's 1 so it's bigger than the 0 fallback value
    # of progress
    water_goal_before = before_data.get("waterGoal", 1)
    calories_goal_before = before_data.get("caloriesGoal", 1)
    exercise_goal_before = before_data.get("exerciseGoal", 1)
    steps_goal_before = before_data.get("stepsGoal", 1)

    water_goal_after = after_data.get("waterGoal", 1)
    calories_goal_after = after_data.get("caloriesGoal", 1)
    exercise_goal_after = after_data.get("exerciseGoal", 1)
    steps_goal_after = after_data.get("stepsGoal", 1)

    # Goal completions, +1 if goal achieved (went to met from not met), -1 if goal failed (went to not met from met, user took back inputs or 
    # increased goal), +0 if unchanged (status stayed the same [still met, or still unmet])
    water_before = before_data.get("waterProgress", 0)
    water_after = after_data.get("waterProgress", 0)
    water_delta = int(water_after >= water_goal_after) - int(water_before >= water_goal_before)

    min_calories_before = calories_goal_before - (calories_goal_before * 0.1)
    max_calories_before = calories_goal_before + (calories_goal_before * 0.1)
    calories_before = before_data.get("caloriesProgress", 0)
    calories_before_in_range = max_calories_before >= calories_before >= min_calories_before

    min_calories_after = calories_goal_after - (calories_goal_after * 0.1)
    max_calories_after = calories_goal_after + (calories_goal_after * 0.1)
    calories_after = after_data.get("caloriesProgress", 0)
    calories_after_in_range = max_calories_after >= calories_after >= min_calories_after

    calories_delta = int(calories_after_in_range) - int(calories_before_in_range)

    exercise_before = before_data.get("exerciseProgress", 0)
    exercise_after = after_data.get("exerciseProgress", 0)
    exercise_delta = int(exercise_after >= exercise_goal_after) - int(exercise_before >= exercise_goal_before)

    steps_before = before_data.get("stepsProgress", 0)
    steps_after = after_data.get("stepsProgress", 0)
    steps_delta = steps_after - steps_before

    # Impossible to take above 100k steps or below 0 steps, between syncs, to avoid cheating
    if steps_delta > 100000: 
        steps_delta = 100000

    if steps_delta < 0 and after_data: # Also checks if the document exists to avoid nullifying delta on document delete
        steps_delta = 0

    steps_goal_delta = int(steps_after >= steps_goal_after) - int(steps_before >= steps_goal_before)

    # Commits to Leaderboards document
    if any([water_delta, calories_delta, exercise_delta, steps_goal_delta, steps_delta]):
        db.collection("leaderboards").document(uid).set({
            "waterGoalsCompleted": Increment(water_delta),
            "caloriesGoalsCompleted": Increment(calories_delta),
            "exerciseGoalsCompleted": Increment(exercise_delta),
            "stepsGoalsCompleted": Increment(steps_goal_delta),
            "totalSteps": Increment(steps_delta)
        }, merge=True)

from firebase_admin import firestore
from firebase_functions import firestore_fn

# Only this four columns are needed for the healthiest user document, all others are filtered
def create_healthiest_user_payload(data):
    return {
        "uid": data.get("uid", ""),
        "healthiestUserScore": data.get("healthiestUserScore", 0),
        "profilePictureString": data.get("profilePictureString", ""),
        "username": data.get("username", "")
    }


@firestore_fn.on_document_written(document="leaderboards/{uid}")
def calculate_healthiest_user(event: firestore_fn.Event[firestore_fn.Change[firestore_fn.DocumentSnapshot]]) -> None:
    db = firestore.client()
    healthiest_user_document = db.collection("app_state").document("healthiest_user")

    # Fetches the user's and healthiest user's data
    user_exists = event.data.after is not None and event.data.after.exists
    user_data = event.data.after.to_dict() if user_exists else {}
    user_uid = event.params["uid"]
    user_data["uid"] = user_uid # Injects the uid, because its in the leaderboards document title not in a column
    user_score = user_data.get("healthiestUserScore", 0)

    healthiest_user_data = healthiest_user_document.get().to_dict()
    healthiest_user_uid = healthiest_user_data.get("uid")
    healthiest_user_score = healthiest_user_data.get("healthiestUserScore", 0)

    # If the healthiest user deleted their account, finds the new healthiest user. Also if the user is already the healthiest user and they lost
    # points (took back inputs or increased some goal), checks if they got overtaken
    if user_uid == healthiest_user_uid and (not user_exists or user_score < healthiest_user_score):
        new_healthiest_user_query = db.collection("leaderboards").order_by("healthiestUserScore", direction = firestore.Query.DESCENDING).limit(1).get()
        new_healthiest_user_data = new_healthiest_user_query[0].to_dict()
        new_healthiest_user_data["uid"] = new_healthiest_user_query[0].id
        healthiest_user_document.set(create_healthiest_user_payload(new_healthiest_user_data))

        return

    # If the healthiest user gained points or if this user suprassed the healthiest user, updates the document
    if user_uid == healthiest_user_uid or user_score > healthiest_user_score:
        healthiest_user_document.set(create_healthiest_user_payload(user_data))

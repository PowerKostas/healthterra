from firebase_admin import firestore
from firebase_functions import firestore_fn

# The user sync function is seperated from the goals sync function, because users will rarely change their profile and there is no reason to fetch
# their user document every time
@firestore_fn.on_document_written(document="users/{uid}")
def perform_leaderboards_user_sync(event: firestore_fn.Event[firestore_fn.Change[firestore_fn.DocumentSnapshot]]) -> None:
    # If the user document was deleted, clean up their leaderboard document
    if not event.data.after or not event.data.after.exists:
        firestore.client().collection("leaderboards").document(event.params["uid"]).delete()
        return
    
    before_data = event.data.before.to_dict() if event.data.before and event.data.before.exists else {}
    after_data = event.data.after.to_dict() if event.data.after and event.data.after.exists else {}

    old_username = before_data.get("username")
    new_username = after_data.get("username")
    
    old_profile_picture_string = before_data.get("profilePictureString")
    new_profile_picture_string = after_data.get("profilePictureString")

    if old_username != new_username or old_profile_picture_string != new_profile_picture_string:
        firestore.client().collection("leaderboards").document(event.params["uid"]).set({
            "username": new_username,
            "profilePictureString": new_profile_picture_string
        }, merge=True)

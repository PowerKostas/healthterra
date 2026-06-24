from firebase_admin import firestore
from firebase_functions import firestore_fn

# The user sync function is separated from the goals sync function, because users will rarely change their profile and there is no reason to fetch
# their user document every time
@firestore_fn.on_document_written(document="users/{uid}")
def perform_leaderboards_user_sync(event: firestore_fn.Event[firestore_fn.Change[firestore_fn.DocumentSnapshot]]) -> None:
    leaderboards_ref = firestore.client().collection("leaderboards").document(event.params["uid"])

    # If the user document was deleted, clean up their leaderboard document
    if not event.data.after or not event.data.after.exists:
        leaderboards_ref.delete()
        return
    
    before_data = event.data.before.to_dict() if event.data.before and event.data.before.exists else {}
    after_data = event.data.after.to_dict() if event.data.after and event.data.after.exists else {}

    old_leaderboards_visibility = before_data.get("leaderboardsVisibility")
    new_leaderboards_visibility = after_data.get("leaderboardsVisibility")

    if new_leaderboards_visibility == "Public":
        old_username = before_data.get("username")
        new_username = after_data.get("username")
        
        old_profile_picture_string = before_data.get("profilePictureString")
        new_profile_picture_string = after_data.get("profilePictureString")

        # Only push a normal update if the user just switched to a public profile or they changed their data while having a public profile
        if old_leaderboards_visibility != new_leaderboards_visibility or old_username != new_username or old_profile_picture_string != new_profile_picture_string:
            leaderboards_ref.set({
                "username": new_username,
                "profilePictureString": new_profile_picture_string
            }, merge=True)

    else:
        # Only push an anonymous update if the user just switched to anonymous or is new
        if old_leaderboards_visibility != new_leaderboards_visibility or not event.data.before.exists:
            leaderboards_ref.set({
                "username": "Anonymous",
                "profilePictureString": "anonymous"
            }, merge=True)

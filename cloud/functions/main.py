from firebase_admin import initialize_app

initialize_app()

from leaderboards_sync import perform_leaderboards_sync

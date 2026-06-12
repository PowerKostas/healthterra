from firebase_admin import initialize_app

initialize_app()

from daily_sync import perform_daily_sync

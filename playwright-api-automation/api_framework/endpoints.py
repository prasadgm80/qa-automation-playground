"""Single place for API routes."""


class Endpoints:
    LOGIN = "/api/auth/login"
    ME = "/api/auth/me"
    EVENTS = "/api/events"

    @staticmethod
    def event(event_id: int) -> str:
        return f"{Endpoints.EVENTS}/{event_id}"

"""Request payload models (pure data, no HTTP logic)."""

from dataclasses import dataclass, field
from typing import Any


@dataclass(frozen=True, slots=True)
class Credentials:
    email: str
    password: str = field(repr=False)  # keep secrets out of logs and reprs

    def to_dict(self) -> dict[str, str]:
        return {"email": self.email, "password": self.password}


@dataclass(frozen=True, slots=True)
class EventPayload:
    title: str
    description: str
    category: str
    venue: str
    city: str
    event_date: str
    price: int
    total_seats: int
    image_url: str

    def to_dict(self) -> dict[str, Any]:
        return {
            "title": self.title,
            "description": self.description,
            "category": self.category,
            "venue": self.venue,
            "city": self.city,
            "eventDate": self.event_date,
            "price": self.price,
            "totalSeats": self.total_seats,
            "imageUrl": self.image_url,
        }

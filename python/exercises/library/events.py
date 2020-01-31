class EventDispatcher:
    def __init__(self):
        self.name = "event dispatcher"

    def dispatch(self, event):
        print(f"{self.name}: {event}")

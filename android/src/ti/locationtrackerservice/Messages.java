package ti.locationtrackerservice;

import org.appcelerator.kroll.KrollDict;

public class Messages {
	public static class TrackerEvent {
		public final KrollDict message;

		public TrackerEvent(KrollDict message) {
			this.message = message;
		}

	}

	public static class NotificationEvent {
		public final KrollDict message;

		public NotificationEvent(KrollDict message) {
			super();
			this.message = message;

		}

	}

	public static class AdapterEvent {
		public final KrollDict message;

		public AdapterEvent(KrollDict message) {
			this.message = message;
		}

	}
}

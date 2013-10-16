package org.finikes.tridge.aio.tcp.session;

public interface SessionIdleType {
	public final static int READ_IDLE = 1;
	public final static int WRITE_IDLE = 2;
	public final static int OPT_IDLE = 3;

	public final static SessionIdleType READ_IDLE_TYPE = new SessionIdleType() {
		@Override
		public int getType() {
			return READ_IDLE;
		}

		@Override
		public boolean isOptIdle() {
			return false;
		}

		@Override
		public boolean isReadIdle() {
			return true;
		}

		@Override
		public boolean isWriteIdle() {
			return false;
		}
	};

	public final static SessionIdleType WRITE_IDLE_TYPE = new SessionIdleType() {
		@Override
		public int getType() {
			return WRITE_IDLE;
		}

		@Override
		public boolean isOptIdle() {
			return false;
		}

		@Override
		public boolean isReadIdle() {
			return false;
		}

		@Override
		public boolean isWriteIdle() {
			return true;
		}
	};

	public final static SessionIdleType OPT_IDLE_TYPE = new SessionIdleType() {
		@Override
		public int getType() {
			return OPT_IDLE;
		}

		@Override
		public boolean isOptIdle() {
			return true;
		}

		@Override
		public boolean isReadIdle() {
			return false;
		}

		@Override
		public boolean isWriteIdle() {
			return false;
		}
	};

	public int getType();

	public boolean isReadIdle();

	public boolean isWriteIdle();

	public boolean isOptIdle();
}

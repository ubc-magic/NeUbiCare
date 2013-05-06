package com.example.audiotowotkit;

public enum ACTIVITY {INVALID(0), QUIET(1), LIGHT(2), HEAVY(3), SOCIAL(4);
		private final int index;
		
		ACTIVITY(int index){
			this.index = index;
		}
		
		public int getIndex() { return index;}
		
		public static ACTIVITY fromIndex(int in){
			for(ACTIVITY act : values()){
				if(in == act.getIndex()){
					return act;
				}
			}
			return INVALID; 
		}
		
		
		public static String getDescription(ACTIVITY a){
			switch(a){
				case QUIET:
					return "Quiet, little to no activity";
				case LIGHT:
					return "Light activity and noise";
				case HEAVY:
					return "Heavy activity and noise";
				case SOCIAL:
					return "Noisy. Some sort of social event";
				default:
					return "Not paying attention";
			}
		
		}
		public static int getNumber(){ return 5;} //MANUALLY COUNTED. IGNORES UNSURE. KEEP UPDATED
	}; 
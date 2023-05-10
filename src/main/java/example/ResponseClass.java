/* Amplify Params - DO NOT EDIT
	ENV
	REGION
	STORAGE_MYBUCKET_BUCKETNAME
Amplify Params - DO NOT EDIT */

package example;
        
     public class ResponseClass {
        String greetings;

        public String getGreetings() {
            return this.greetings;
        }

        public void setGreetings(String greetings) {
            this.greetings = greetings;
        }

        public ResponseClass(String greetings) {
            this.greetings = greetings;
        }
    }
package Models.MessageModels.Register;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by MARKAN on 26.05.2017.
 */
    public class Message
    {
        @SerializedName("Message")
        public String message;
        @SerializedName("ModelState")
        public ModelState modelState;

        public String getMessage()
        {
            StringBuilder builder=new StringBuilder();
            for (String s:modelState.messages) {
                builder.append(s.concat("\n"));
            }
            return builder.toString();
        }

}
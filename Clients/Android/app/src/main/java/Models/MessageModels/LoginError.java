package Models.MessageModels;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MARKAN on 26.05.2017.
 */
public class LoginError {
    @SerializedName("error")
    public String error;
    @SerializedName("error_description")
    public String errorDescription;
}

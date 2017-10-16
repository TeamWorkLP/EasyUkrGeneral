package MVP.Presenters;

import Hardware.WiFiConnector;
import Infrastructure.RESTful.Autorization.AuthorizationService;
import Infrastructure.Static.EasyUkrApplication;
import MVP.Views.IView;
import Models.AutorizationModels.Abstract.UserModel;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import com.example.mark0.easyukrainian.ProfileNewActivity;

import static Infrastructure.Static.EasyUkrApplication.showToast;


/**
 * Created by mark0 on 26.04.2017.
 */
public class LoginPresenter extends AutorizationPresenter {

    public LoginPresenter() {
        model = new UserModel();
    }
    private Dialog dialog=null;
    public void login() {
        if (checkModel()) {
            dialog = EasyUkrApplication.initDialog(view.getCurrentContext());
            dialog.setCancelable(false);
            dialog.show();
            if ((new WiFiConnector(view.getCurrentContext().getBaseContext()).isConnected())) {
                LoginAsync loginAsync = new LoginAsync(view.getCurrentContext(), model);
                loginAsync.execute();
            } else
                showToast(view.getCurrentContext(), "Wifi isn`t connected");
            dialog.create();
        }
        else
        {
            showToast(view.getCurrentContext(), "Fields aren`t filled");
        }
    }

    @Override
    public IView getView() {
        return view;
    }

    @Override
    public void setView(IView view) {
        this.view = view;
        activity = view.getCurrentContext();
    }

    private class LoginAsync extends AsyncTask<Void, Void, Void> {
        private Activity contex;
        private UserModel model;
        private AuthorizationService service;

        public LoginAsync(Activity context, UserModel model) {
            this.model = model;
            this.contex = context;
            service = new AuthorizationService(context);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.hide();
            dialog.dismiss();
            if (service.isSuccessful) {
                redirectView(ProfileNewActivity.class, null);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                service.login(model, true);
            } catch (final Exception ex) {
                contex.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(contex.getBaseContext(), ex.getMessage());
                    }
                });
            }
            return null;
        }
    }
}

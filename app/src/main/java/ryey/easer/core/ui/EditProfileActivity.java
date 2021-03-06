package ryey.easer.core.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ryey.easer.R;
import ryey.easer.commons.plugindef.StorageData;
import ryey.easer.commons.plugindef.operationplugin.OperationData;
import ryey.easer.commons.plugindef.operationplugin.OperationPlugin;
import ryey.easer.core.data.ProfileStructure;
import ryey.easer.core.data.storage.ProfileDataStorage;
import ryey.easer.core.data.storage.xml.profile.XmlProfileDataStorage;
import ryey.easer.plugins.PluginRegistry;

import static ryey.easer.core.ui.EditDataProto.CONTENT_NAME;

public class EditProfileActivity extends AppCompatActivity {

    ProfileDataStorage storage = null;

    EditDataProto.Purpose purpose;
    String oldName = null;

    EditText mEditText = null;

    Map<String, SwitchItemLayout> items = new HashMap<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_data, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                alterProfile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            storage = XmlProfileDataStorage.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        purpose = (EditDataProto.Purpose) getIntent().getSerializableExtra(EditDataProto.PURPOSE);
        if (purpose != EditDataProto.Purpose.add)
            oldName = getIntent().getStringExtra(CONTENT_NAME);
        if (purpose == EditDataProto.Purpose.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    setResult(RESULT_CANCELED);
                    dialog.cancel();
                }
            }).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alterProfile();
                }
            });
            builder.setMessage(getString(R.string.prompt_delete, oldName));
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            setTheme(R.style.AppTheme_ActivityDialog);
            builder.show();
        } else {
            setContentView(R.layout.activity_edit_profile);
            ActionBar actionbar = getSupportActionBar();
            actionbar.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            actionbar.setDisplayHomeAsUpEnabled(true);
            setTitle(R.string.title_edit_profile);
            init();
            if (purpose == EditDataProto.Purpose.edit) {
                ProfileStructure profile = storage.get(oldName);
                loadFromProfile(profile);
            }
        }
    }

    void init() {
        mEditText = (EditText) findViewById(R.id.editText_profile_title);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_profiles);
        for (OperationPlugin operationPlugin : PluginRegistry.getInstance().getOperationPlugins()) {
            SwitchItemLayout view = new SwitchItemLayout(this, operationPlugin.view(this));
            layout.addView(view);
            items.put(operationPlugin.name(), view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        storage = null;
    }

    protected void loadFromProfile(ProfileStructure profile) {
        mEditText.setText(oldName);

        for (OperationPlugin plugin : PluginRegistry.getInstance().getOperationPlugins()) {
            SwitchItemLayout item = items.get(plugin.name());
            item.fill(profile.get(plugin.name()));
        }
    }

    protected ProfileStructure saveToProfile() {
        ProfileStructure profile = new ProfileStructure(mEditText.getText().toString());

        for (OperationPlugin plugin : PluginRegistry.getInstance().getOperationPlugins()) {
            SwitchItemLayout item = items.get(plugin.name());
            StorageData data = item.getData();
            if (data == null)
                continue;
            if (data instanceof OperationData) {
                if (data.isValid())
                    profile.set(plugin.name(), (OperationData) data);
            } else {
                Log.wtf(getClass().getSimpleName(), "data of plugin's Layout is not instance of OperationData");
                throw new RuntimeException("data of plugin's Layout is not instance of OperationData");
            }
        }

        return profile;
    }

    protected boolean alterProfile() {
        boolean success;
        if (purpose == EditDataProto.Purpose.delete)
            success = storage.delete(oldName);
        else {
            ProfileStructure newProfile = saveToProfile();
            if (!newProfile.isValid()) {
                return false;
            }
            switch (purpose) {
                case add:
                    success = storage.add(newProfile);
                    break;
                case edit:
                    success = storage.edit(oldName, newProfile);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown Purpose");
            }
        }
        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.prompt_save_failed), Toast.LENGTH_SHORT).show();
        }
        return success;
    }
}

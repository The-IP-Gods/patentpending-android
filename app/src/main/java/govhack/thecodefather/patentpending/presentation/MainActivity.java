package govhack.thecodefather.patentpending.presentation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import govhack.thecodefather.patentpending.R;
import govhack.thecodefather.patentpending.data.api.HttpCallback;
import govhack.thecodefather.patentpending.data.api.PatentPendingApiClient;
import govhack.thecodefather.patentpending.data.models.GodDataModel;
import govhack.thecodefather.patentpending.data.models.PatentDataModel;
import govhack.thecodefather.patentpending.presentation.adapter.RecyclerViewSearchAdapter;
import lombok.val;
import retrofit2.Call;
import retrofit2.Response;

@Fullscreen
@EActivity(R.layout.activity_main)
public class MainActivity extends ActivityBase {

    @ViewById(R.id.root_view)
    CoordinatorLayout rootView;

    @ViewById(R.id.tvEmptyView)
    TextView tvEmptyView;

    @ViewById(R.id.llWelcomeView)
    LinearLayout llWelcomeView;

    @ViewById(R.id.progress_overlay)
    FrameLayout progressOverlay;

    @ViewById
    RecyclerView rvSearchResults;
    private RecyclerViewSearchAdapter rvSearchAdapter;
    private List<PatentDataModel> mPatents = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        val inflater = getMenuInflater();
        inflater.inflate(R.menu.main_search_view, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);

        val searchViewAndroidActionBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchViewAndroidActionBar.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        submitSearchRequest(query);
                        searchViewAndroidActionBar.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }

    @AfterViews
    void initPatentsList() {
        rvSearchAdapter = new RecyclerViewSearchAdapter(mPatents, this);
        rvSearchResults.setAdapter(rvSearchAdapter);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
    }

    @Background
    void submitSearchRequest(@Nullable String query) {
        if (!TextUtils.isEmpty(query)) {
            isProgressOverlayVisible(true);

            PatentPendingApiClient.searchPatents(query, new HttpCallback<GodDataModel>() {
                @Override
                public void onSuccess(Call<GodDataModel> call, Response<GodDataModel> response) {
                    val patentDataModels = getPayload().getPatents();
                    updatePatents(patentDataModels);
                }

                @Override
                public void onError(Call<GodDataModel> call, Response<GodDataModel> response) {
                    showSnackbar("Uh-oh an error occurred, please retry");
                }

                @Override
                public void onFailure(Call<GodDataModel> call) {
                    showSnackbar("Uh-oh an error occurred, please retry");
                }

                @Override
                protected void always() {
                    isProgressOverlayVisible(false);
                }
            });
        } else {
            updatePatents(null);
        }
    }

    @UiThread
    void isProgressOverlayVisible(boolean b) {
        progressOverlay.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @UiThread
    void updatePatents(@Nullable ImmutableList<PatentDataModel> patents) {
        llWelcomeView.setVisibility(View.GONE);
        if (null==patents || patents.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
            rvSearchAdapter.updatePatents(patents);
        }
    }

    public void showSnackbar(@NonNull String s) {
        showSnackbar(rootView, s);
    }

    @UiThread
    void showSnackbar(@NonNull String s, int duration) {
        Snackbar snackbar = Snackbar.make(rootView, s, duration);
        snackbar.show();
    }

}

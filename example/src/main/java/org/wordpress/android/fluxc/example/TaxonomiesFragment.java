package org.wordpress.android.fluxc.example;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.TaxonomyActionBuilder;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.TermModel;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.fluxc.store.TaxonomyStore;
import org.wordpress.android.fluxc.store.TaxonomyStore.OnTaxonomyChanged;
import org.wordpress.android.fluxc.store.TaxonomyStore.OnTermInstantiated;
import org.wordpress.android.fluxc.store.TaxonomyStore.OnTermUploaded;
import org.wordpress.android.fluxc.store.TaxonomyStore.RemoteTermPayload;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static junit.framework.Assert.assertTrue;

public class TaxonomiesFragment extends Fragment {
    @Inject SiteStore mSiteStore;
    @Inject TaxonomyStore mTaxonomyStore;
    @Inject Dispatcher mDispatcher;

    // Needed for instantiate action :/
    private TermModel mNewTerm;
    private CountDownLatch mCountDownLatch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ExampleApp) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_taxonomies, container, false);
        view.findViewById(R.id.fetch_categories).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCategoriesFirstSite();
            }
        });
        view.findViewById(R.id.fetch_tags).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTagsFirstSite();
            }
        });
        view.findViewById(R.id.create_category).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                createCategory();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDispatcher.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDispatcher.unregister(this);
    }

    private SiteModel getFirstSite() {
        return mSiteStore.getSites().get(0);
    }

    private void fetchCategoriesFirstSite() {
        mDispatcher.dispatch(TaxonomyActionBuilder.newFetchCategoriesAction(getFirstSite()));
    }

    private void fetchTagsFirstSite() {
        mDispatcher.dispatch(TaxonomyActionBuilder.newFetchTagsAction(getFirstSite()));
    }

    private void createCategory() {
        // Count down latch used for waiting for the asynchronous Instantiate action.
        mCountDownLatch = new CountDownLatch(1);
        mDispatcher.dispatch(TaxonomyActionBuilder.newInstantiateCategoryAction(getFirstSite()));
        try {
            assertTrue(mCountDownLatch.await(2, TimeUnit.SECONDS));
            mNewTerm.setName("FluxC-category-" + new Random().nextLong());
            mNewTerm.setDescription("From FluxC example app");
            RemoteTermPayload payload = new RemoteTermPayload(mNewTerm, getFirstSite());
            mDispatcher.dispatch(TaxonomyActionBuilder.newPushTermAction(payload));
        } catch (Exception e) {
            // noop
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTaxonomyChanged(OnTaxonomyChanged event) {
        AppLog.i(T.API, "OnTaxonomyChanged: rowsAffected=" + event.rowsAffected);
        if (event.isError()) {
            String error = "Error: " + event.error.type + " - " + event.error.message;
            prependToLog(error);
            AppLog.i(T.TESTS, error);
        } else {
            List<TermModel> terms = mTaxonomyStore.getTermsForSite(getFirstSite(), event.taxonomyName);
            for (TermModel term : terms) {
                prependToLog(event.taxonomyName + " " + term.getRemoteTermId() + ": " + term.getName());
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onTermInstantiated(OnTermInstantiated event) {
        mNewTerm = event.term;
        mCountDownLatch.countDown();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTermUploaded(OnTermUploaded event) {
        prependToLog("Term uploaded! Remote category id: " + event.term.getRemoteTermId());
    }

    private void prependToLog(final String s) {
        ((MainExampleActivity) getActivity()).prependToLog(s);
    }
}
package com.fabiofilho.popmovies.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.fabiofilho.popmovies.Activities.MovieDetailsActivity;
import com.fabiofilho.popmovies.Objects.Connections.AsyncTaskRequest;
import com.fabiofilho.popmovies.Objects.Connections.NetworkUtils;
import com.fabiofilho.popmovies.Objects.Dialogs.MovieOrderDialog;
import com.fabiofilho.popmovies.Objects.Movies.Movie;
import com.fabiofilho.popmovies.Objects.Movies.MovieAdapter;
import com.fabiofilho.popmovies.Objects.Movies.MovieJSONUtil;
import com.fabiofilho.popmovies.Objects.Utils;
import com.fabiofilho.popmovies.R;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainFragment extends Fragment implements View.OnClickListener{

    public final String SAVED_INSTANCE_KEY_MOVIE_ORDER = "SAVED_INSTANCE_KEY_MOVIE_ORDER";

    private View mRootView;
    private GridView mGridView;
    private AsyncTaskRequest mAsyncTaskRequest;
    private int mIndexMovieOrderChosen = 0;
    private ProgressBar mProgressBar;

    private LinearLayout mLinearLayoutNoInternetWarning;
    private Button mButtonNoInternetTryAgain;

    public MainFragment() {

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_movie_sort) {

            // Opens the movie dialog order.
            openMovieDialogOrder();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Loads the saved variables.
        loadSavedInstanceState(savedInstanceState);

        // Refers the objects to class variables.
        referScreenObjects();

        // Setting the default values for them.
        loadObjects();

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Saves the current movie order.
        outState.putInt(SAVED_INSTANCE_KEY_MOVIE_ORDER, mIndexMovieOrderChosen);

        super.onSaveInstanceState(outState);
    }

    /**
     * Loads the saved variables.
     * @param savedInstanceState
     */
    private void loadSavedInstanceState(@Nullable Bundle savedInstanceState) {

        if(savedInstanceState != null){

            // Loads the movie order.
            mIndexMovieOrderChosen = savedInstanceState.getInt(SAVED_INSTANCE_KEY_MOVIE_ORDER);
        }

    }

    /**
     * Loads the properties from the objects.
     */
    private void loadObjects() {

        setObjectsListeners();
        updateMoviesAdapter(Movie.MOVIE_ORDER[mIndexMovieOrderChosen]);
    }

    /**
     * Refers the objects from screen.
     */
    private void referScreenObjects() {

        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.FragmentMainMoviesProgressBar);
        mGridView = (GridView) mRootView.findViewById(R.id.FragmentMainMoviesGridView);

        mLinearLayoutNoInternetWarning = (LinearLayout) mRootView.findViewById(R.id.FragmentMainMoviesNoInternetWarningLinearLayout);
        mButtonNoInternetTryAgain = (Button) mRootView.findViewById(R.id.FragmentMainMoviesNoInternetTryAgainButton);
    }

    /**
     * Defines all objects events.
     */
    private void setObjectsListeners(){

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                openMovieDetails(position);
            }
        });

        /*mButtonNoInternetTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(Utils.getMethodName(), "CLICKED");
                // Tries to update the adapter if the user wants.
                updateMoviesAdapter(Movie.MOVIE_ORDER[mIndexMovieOrderChosen]);
            }
        });*/

    }


    /**
     * Opens the movie details activity and sends an instance of Movie class
     * chosen by user.
     * @param position
     */
    private void openMovieDetails(int position){

        // Casts the Movie instance by the position to send it through intent to MovieDetailsActivity.
        Movie movie = ((Movie) mGridView.getAdapter().getItem(position));

        // Creates an intent with a Movie instance as parameter.
        Intent intent = new Intent(mRootView.getContext(), MovieDetailsActivity.class);
        intent.putExtra(Movie.PARCELABLE_KEY, movie);

        startActivity(intent);
    }


    /**
     * Create an instance of MovieOrderDialog class and shows it to the user.
     */
    private void openMovieDialogOrder(){

        MovieOrderDialog movieOrderDialog = new MovieOrderDialog();
        movieOrderDialog.onCreateDialog(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Updates the movies adapter if the user has chosen a different order.
                updateMoviesAdapter(Movie.MOVIE_ORDER[which]);
            }
        }).show();
    }

    /***
     *  Download the movies content and update the MoviesAdapter with this data.
     * @param movieOrder
     */
    private void updateMoviesAdapter(String movieOrder) {

        try {
            // Checks for internet connection.
            /*if (NetworkUtils.isNetworkAvailable(mRootView.getContext())){
                setVisibilityNoInternetWarning(false);
            }else{
                setVisibilityNoInternetWarning(true);
                return;
            }*/

            URL url = NetworkUtils.buildURL(Movie.MOVIES_URL + movieOrder, true);
            mAsyncTaskRequest = new AsyncTaskRequest() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(String response) {
                    super.onPostExecute(response);

                    try {
                        // Checks if the fragment is attached to activity.
                        if (isAdded()) {

                            // Checks if the response from http request is different of null to load
                            // the data on the grid view.
                            if (response != null) {

                                ArrayList<Movie> movieList;
                                movieList = (ArrayList<Movie>) MovieJSONUtil.createMovieListByJSON(response);

                                mGridView.setAdapter(
                                        new MovieAdapter(
                                                mRootView.getContext(),
                                                movieList
                                        )
                                );
                            }else{
                                setVisibilityNoInternetWarning(true);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e(Utils.getMethodName(), e.toString());
                    }
                    finally {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

            };

            // Initialize the parallel process to load content.
            mAsyncTaskRequest.execute(url);

        }catch (MalformedURLException e){
            Log.e(Utils.getMethodName(), e.toString());
        }
    }

    /**
     * Sets the visibility of the layout that warns the user when there isn't a internet connection.
     * @param value objects status visibility.
     */
    private void setVisibilityNoInternetWarning(boolean value){

        if(value){
            // Sets the objects visible when there isn't internet connection.
            mLinearLayoutNoInternetWarning.setVisibility(View.VISIBLE);

        }else {
            // Sets the objects invisible when there is internet connection.
            mLinearLayoutNoInternetWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {

        if( view.getId() == R.id.FragmentMainMoviesNoInternetTryAgainButton ){
            Log.e(Utils.getMethodName(), "CLICKED");
        }
    }
}

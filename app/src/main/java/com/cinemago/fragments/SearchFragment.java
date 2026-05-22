package com.cinemago.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.activities.MovieDetailActivity;
import com.cinemago.adapters.MovieAdapter;
import com.cinemago.api.RetrofitClient;
import com.cinemago.models.MovieResponse;
import com.cinemago.utils.Constants;
import java.util.ArrayList;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private MovieAdapter adapter;
    private ProgressBar progressBar;
    private SpeechRecognizer speechRecognizer;
    private ImageButton btnMic;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startVoiceSearch();
                } else {
                    Toast.makeText(getContext(), "Microphone permission is required for voice search", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.et_search);
        btnMic = view.findViewById(R.id.btn_mic);
        progressBar = view.findViewById(R.id.progress_bar);

        RecyclerView rv = view.findViewById(R.id.rv_results);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MovieAdapter(getContext(), new ArrayList<>(), movie -> {
            Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
            intent.putExtra(Constants.EXTRA_MOVIE_ID, movie.getId());
            intent.putExtra(Constants.EXTRA_MOVIE_TITLE, movie.getTitle());
            intent.putExtra(Constants.EXTRA_MOVIE_POSTER, movie.getPosterPath());
            intent.putExtra(Constants.EXTRA_MOVIE_OVERVIEW, movie.getOverview());
            intent.putExtra(Constants.EXTRA_MOVIE_RATING, movie.getVoteAverage());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            private android.os.Handler handler = new android.os.Handler();
            private Runnable runnable;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (runnable != null) handler.removeCallbacks(runnable);
                runnable = () -> {
                    if (s.toString().trim().length() > 2)
                        searchMovies(s.toString().trim());
                };
                handler.postDelayed(runnable, 600);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnMic.setOnClickListener(v -> {
            if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
                Toast.makeText(getContext(), "Voice search not supported on this device", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startVoiceSearch();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        return view;
    }

    private void startVoiceSearch() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                btnMic.setImageResource(R.drawable.ic_mic_active);
                Toast.makeText(getContext(), "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                btnMic.setImageResource(R.drawable.ic_mic);
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    etSearch.setText(matches.get(0));
                    searchMovies(matches.get(0));
                }
            }

            @Override
            public void onError(int error) {
                btnMic.setImageResource(R.drawable.ic_mic);
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH: message = "Didn't hear anything"; break;
                    case SpeechRecognizer.ERROR_NETWORK: message = "Network error"; break;
                    default: message = "Voice error, please try again"; break;
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private void searchMovies(String query) {
        if (!isAdded()) return;
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance().getApiService()
                .searchMovies(Constants.TMDB_API_KEY, query)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                        if (isAdded()) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null)
                                adapter.updateMovies(response.body().getResults());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (speechRecognizer != null) speechRecognizer.destroy();
    }
}
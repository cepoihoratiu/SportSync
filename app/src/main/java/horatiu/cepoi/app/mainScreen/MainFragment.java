package horatiu.cepoi.app.mainScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import horatiu.cepoi.app.R;

public class MainFragment extends Fragment {
    private Fragment homepageFragment;
    private String userId;

    public MainFragment(Fragment homepageFragment, String userId) {
        this.homepageFragment = homepageFragment;
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav);
        if (userId.equals("3VDP7Aoj2cT5DNHSu4t6")) {
            bottomNav.getMenu().findItem(R.id.nav_admin).setVisible(true);
        }

        // ✅ Load homepage with userId
        loadFragment(homepageFragment != null ? homepageFragment : HomepageFragment.newInstance(userId));

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = homepageFragment != null ? homepageFragment : HomepageFragment.newInstance(userId);
            } else if (id == R.id.nav_ml) {
                selectedFragment = MLFragment.newInstance(userId);
            } else if (id == R.id.nav_profile) {
                selectedFragment = ProfileFragment.newInstance(userId);
            } else if (id == R.id.nav_chat) {
                selectedFragment = ChatFragment.newInstance(userId);
            } else if (id == R.id.nav_admin && userId.equals("3VDP7Aoj2cT5DNHSu4t6")) {
                selectedFragment = new AdminUsersFragment(userId);
            }

            return loadFragment(selectedFragment);
        });

        return view;
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getChildFragmentManager() // ✅ child manager since we're in a fragment
                    .beginTransaction()
                    .replace(R.id.fragment_container_main, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}

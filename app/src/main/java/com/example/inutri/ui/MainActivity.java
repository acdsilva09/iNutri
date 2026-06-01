package com.example.inutri.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.inutri.R;
import com.example.inutri.databinding.ActivityMainBinding;
import com.example.inutri.ui.capture.CaptureMealActivity;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // destinos top-level do seu grafo de navegação
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_alimentos
        ).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_content_main);

        // título e botão Up integrados à ActionBar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        // itens do drawer navegando no NavController
        NavigationUI.setupWithNavController(navigationView, navController);

        // Configura o FAB para abrir a tela de Adicionar Refeição
        binding.includeAppBarMain.fab.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.inutri.ui.meal.AddMealActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu); // deve conter action_capture_meal (opcional)
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_capture_meal) {
            startActivity(new Intent(this, CaptureMealActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}

package com.york.uihomework.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.york.data.local.entity.Pokemon
import com.york.uihomework.R
import com.york.uihomework.ui.theme.UIHomeworkTheme
import com.york.uihomework.util.debounceClickable
import com.york.uihomework.util.spacing
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : ComponentActivity() {

    private val viewModel: DetailViewModel by viewModel()


    companion object {
        const val INPUT_EXTRA_POKEMON = "INPUT_EXTRA_POKEMON"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UIHomeworkTheme {
                val pokemon by viewModel.pokemonDetail.collectAsState()
                val evolvesFrom by viewModel.evolvesFrom.collectAsState()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            title = {
                                Text("")
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Finish Detail Page"
                                    )
                                }
                            },
                            actions = {
                                Text(
                                    text = "#${pokemon.pokemonId}",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .padding(end = MaterialTheme.spacing.medium)
                                )
                            }
                        )
                    },
                ) { innerPadding ->
                    val pokemonTypes by produceState<List<String>>(initialValue = emptyList()) {
                        value = viewModel.getPokemonTypes()
                    }
                    DetailPage(
                        pokemon = pokemon,
                        pokemonTypes = pokemonTypes,
                        evolvesFrom = evolvesFrom,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                    ) { evolvesFromPokemon ->
                        Intent(this, DetailActivity::class.java)
                            .apply {
                                putExtra(INPUT_EXTRA_POKEMON, evolvesFromPokemon)
                            }.let {
                                startActivity(it)
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPage(
    pokemon: Pokemon,
    evolvesFrom: Pokemon?,
    pokemonTypes: List<String>,
    modifier: Modifier = Modifier,
    onEvolvesFromClick: (Pokemon) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large)
    ) {
        AsyncImage(
            model = pokemon.image,
            contentDescription = "Pokemon Image of ${pokemon.pokemonName}",
            modifier = Modifier
                .padding(top = MaterialTheme.spacing.medium)
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally),
            error = painterResource(R.drawable.poke_ball_bg),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = pokemon.pokemonName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = MaterialTheme.spacing.medium)
        )
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            pokemonTypes.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(1.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        evolvesFrom?.let {
            Row (
                modifier = Modifier
                    .padding(top = MaterialTheme.spacing.large)
                    .fillMaxWidth()
                    .debounceClickable(
                        indication = null
                    ) {
                        onEvolvesFromClick(it)
                    },
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)){
                    Text(
                        text = "Evolves From",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = it.pokemonName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                AsyncImage(
                    model = it.image,
                    contentDescription = "Pokemon Image of Evolves From ${it.pokemonName}",
                    modifier = Modifier
                        .size(80.dp)
                        .aspectRatio(1f),
                    error = painterResource(R.drawable.poke_ball_bg),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        pokemon.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = MaterialTheme.spacing.large)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun DetailPagePreview() {
    DetailPage(
        Pokemon(
            pokemonId = 2,
            pokemonName = "ivysaur",
            image = "",
            description = "When the bulb on\n" +
                    "its back grows\n" +
                    "large, it appears to lose the\n" +
                    "ability to stand\n" +
                    "on its hind legs., evolvesFrom=bulbasaur",
            evolvesFrom = "magneton"
        ),
        Pokemon(
            pokemonId = 1,
            pokemonName = "bulbasaur",
            image = "",
            description = null,
            evolvesFrom = ""
        ),
        listOf("grass", "poison")
    ) {}
}
package com.york.uihomework

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.york.data.local.entity.Pokemon
import com.york.uihomework.detail.DetailActivity
import com.york.uihomework.ui.theme.UIHomeworkTheme
import com.york.uihomework.util.debounceClickable
import com.york.uihomework.util.spacing
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    companion object {
        const val OUTPUT_EXTRA_POKEMON = "OUTPUT_EXTRA_POKEMON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UIHomeworkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UiPokemonHomepage(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    ) { pokemon ->
                        Intent(this, DetailActivity::class.java)
                            .apply {
                                putExtra(OUTPUT_EXTRA_POKEMON, pokemon)
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
fun UiPokemonHomepage(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onItemClick: (Pokemon) -> Unit
) {
    val typeWithPokemonList by viewModel.typeWithPokemonsFlow.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val capturedWithPokemon by viewModel.capturedWithPokemonFlow.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    LazyColumn(modifier = modifier) {
        item {
            HomeSection(
                typeName = stringResource(R.string.title_my_pocket),
                count = capturedWithPokemon.size,
                pokemonList = capturedWithPokemon.map { it.pokemon },
                onBtnClick = { _, index ->
                    capturedWithPokemon.getOrNull(index)?.let {
                        viewModel.releasePokemon(it.captured)
                    }
                },
                onItemClick = onItemClick
            )
        }
        items(typeWithPokemonList) { typeWithPokemons ->
            HomeSection(
                typeName = typeWithPokemons.type.typeName,
                count = typeWithPokemons.pokemons.size,
                pokemonList = typeWithPokemons.pokemons,
                onBtnClick = { pokemon, _ ->
                    viewModel.capturePokemon(pokemon.pokemonName)
                },
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun HomeSection(
    typeName: String,
    count: Int,
    pokemonList: List<Pokemon>,
    onBtnClick: (Pokemon, Int) -> Unit,
    onItemClick: (Pokemon) -> Unit
) {
    Column {
        TypeHeader(
            typeName = typeName,
            count = count,
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
        ) {
            itemsIndexed(pokemonList) { index, pokemon ->
                PokemonItem(
                    img = pokemon.image,
                    name = pokemon.pokemonName,
                    modifier = Modifier.width(getImgSize()),
                    onBtnClick = {
                        onBtnClick(pokemon, index)
                    },
                    onItemClick = {
                        onItemClick(pokemon)
                    }
                )
            }
        }
    }
}

@Composable
private fun TypeHeader(
    typeName: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = typeName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun PokemonItem(
    img: String?,
    name: String,
    modifier: Modifier = Modifier,
    onBtnClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Column(modifier = modifier.debounceClickable {
        onItemClick()
    }) {
        BoxWithConstraints(modifier.wrapContentSize()) {
            val padding = maxWidth / 16
            AsyncImage(
                model = img,
                contentDescription = "Pokemon Image of $name",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                error = painterResource(R.drawable.poke_ball_bg),
                contentScale = ContentScale.Crop
            )
            Icon(
                painter = painterResource(R.drawable.poke_ball_icon),
                contentDescription = "Pokemon ball Icon",
                tint = Color.Red,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(0.25f)
                    .align(Alignment.TopEnd)
                    .clickable {
                        onBtnClick()
                    }
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.small)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun getImgSize(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val spaceStart = MaterialTheme.spacing.medium
    val spaceDivider = MaterialTheme.spacing.small
    val orientation = LocalConfiguration.current.orientation
    return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Portrait
        (screenWidth - spaceStart - 3 * spaceDivider) / 3.5f
    } else {
        // Landscape
        (screenWidth * 0.8f - spaceStart - 2 * spaceDivider) / 3
    }
}

@Preview(showBackground = true)
@Composable
private fun TypeHeaderPreview() {
    TypeHeader(
        "electric", 9, Modifier.padding(horizontal = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun PokemonItemPreview() {
    PokemonItem(
        img = "",
        name = "Pikachu",
        modifier = Modifier.width(getImgSize()),
        {},{}
    )
}
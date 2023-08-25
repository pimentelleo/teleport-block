// Plugin Bukkit que permite ao jogador construir um portal para outra localização
// O plugin usa Gradle para compilar
// O jogador deve craftar dois blocos de teleporte usando uma esmeralda e duas obsidianas
// O jogador deve colocar o primeiro bloco no local de destino e o segundo no local de origem
// O portal é ativado quando o jogador entra no bloco de origem

package com.example.teleportplugin;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportPlugin extends JavaPlugin implements Listener {

    // O nome do bloco de teleporte
    private static final String TELEPORT_BLOCK_NAME = "Teleport Block";

    // O mapa que guarda as localizações dos portais
    private HashMap<Location, Location> portalMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Registra o plugin como um listener de eventos
        Bukkit.getPluginManager().registerEvents(this, this);

        // Cria a receita do bloco de teleporte
        createTeleportBlockRecipe();
    }

    @Override
    public void onDisable() {
        // Limpa o mapa dos portais
        portalMap.clear();
    }

    // Cria a receita do bloco de teleporte usando uma esmeralda e duas obsidianas
    private void createTeleportBlockRecipe() {
        // Cria um item stack do bloco de teleporte
        ItemStack teleportBlock = new ItemStack(Material.OBSIDIAN);
        ItemMeta meta = teleportBlock.getItemMeta();
        meta.setDisplayName(TELEPORT_BLOCK_NAME);
        teleportBlock.setItemMeta(meta);

        // Cria uma chave para a receita
        NamespacedKey key = new NamespacedKey(this, "teleport_block");

        // Cria uma receita em forma de cruz usando uma esmeralda no meio e duas obsidianas em cima e em baixo
        ShapedRecipe recipe = new ShapedRecipe(key, teleportBlock);
        recipe.shape("O", "E", "O");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('E', Material.EMERALD);

        // Adiciona a receita ao servidor
        Bukkit.addRecipe(recipe);
    }

    // Trata o evento de colocar um bloco no mundo
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Obtém o bloco colocado, o jogador que colocou e o item usado
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        // Verifica se o item é um bloco de teleporte
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(TELEPORT_BLOCK_NAME)) {
            // Obtém a localização do bloco colocado
            Location location = block.getLocation();

            // Verifica se o jogador já tem um portal em construção
            if (portalMap.containsKey(player)) {
                // Obtém a localização do primeiro bloco colocado pelo jogador
                Location destination = portalMap.get(player);

                // Remove o jogador do mapa dos portais em construção
                portalMap.remove(player);

                // Adiciona as duas localizações ao mapa dos portais prontos
                portalMap.put(location, destination);
                portalMap.put(destination, location);

                // Envia uma mensagem ao jogador informando que o portal foi criado com sucesso
                player.sendMessage("Você criou um portal entre " + locationToString(location) + " e " + locationToString(destination));
            } else {
                // Adiciona o jogador e a localização ao mapa dos portais em construção
                portalMap.put((Location) player, location);
                // Envia uma mensagem ao jogador informando que ele deve colocar o segundo bloco em outro lugar para completar o portal
                player.sendMessage("Você colocou o primeiro bloco do portal em " + locationToString(location) + ". Coloque o segundo bloco em outro lugar para completar o portal.");
            }
        }
    }

    // Trata o evento de mover um jogador no mundo
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Obtém o jogador que se moveu e o bloco em que ele está
        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();

        // Verifica se o bloco é um bloco de teleporte
        if (block.getType() == Material.OBSIDIAN && block.hasMetadata(TELEPORT_BLOCK_NAME)) {
            // Obtém a localização do bloco
            Location location = block.getLocation();

            // Verifica se o bloco faz parte de um portal pronto
            if (portalMap.containsKey(location)) {
                // Obtém a localização de destino do portal
                Location destination = portalMap.get(location);

                // Teleporta o jogador para a localização de destino
                player.teleport(destination);

                // Envia uma mensagem ao jogador informando que ele foi teleportado
                player.sendMessage("Você foi teleportado para " + locationToString(destination));
            }
        }
    }

    // Converte uma localização em uma string no formato (x, y, z)
    private String locationToString(Location location) {
        return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }
}
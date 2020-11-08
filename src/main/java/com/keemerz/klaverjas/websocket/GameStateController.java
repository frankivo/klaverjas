package com.keemerz.klaverjas.websocket;

import com.keemerz.klaverjas.converter.GameStateToPlayerGameStateConverter;
import com.keemerz.klaverjas.domain.ActiveGame;
import com.keemerz.klaverjas.domain.GameState;
import com.keemerz.klaverjas.domain.Player;
import com.keemerz.klaverjas.domain.PlayerGameState;
import com.keemerz.klaverjas.repository.ActiveGamesRepository;
import com.keemerz.klaverjas.repository.GameStateRepository;
import com.keemerz.klaverjas.repository.PlayerRepository;
import com.keemerz.klaverjas.websocket.inbound.GameJoinMessage;
import com.keemerz.klaverjas.websocket.inbound.GameLeaveMessage;
import com.keemerz.klaverjas.websocket.inbound.GameStartMessage;
import com.keemerz.klaverjas.websocket.outbound.ActiveGamesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class GameStateController {

    @Autowired
    private SimpMessagingTemplate webSocket;

    private GameStateRepository gameStateRepository = GameStateRepository.getInstance();

    @MessageMapping("/game/start")
    public void startGame(GameStartMessage message, Principal principal) {
        Player sendingPlayer = PlayerRepository.getInstance().getPlayerByUserId(principal.getName());

        GameState gameState = GameState.createNewGame();
        gameState.fillSeat(sendingPlayer);
        gameStateRepository.updateGameState(gameState);

        updateGameStateForAllPlayers(sendingPlayer.getPlayerId(), gameState);

        List<ActiveGame> activeGames = ActiveGamesRepository.getInstance().getActiveGames();
        webSocket.convertAndSend("/topic/lobby", new ActiveGamesMessage(activeGames));

    }

    @MessageMapping("/game/join")
    public void joinGame(GameJoinMessage message, Principal principal) {
        Player sendingPlayer = PlayerRepository.getInstance().getPlayerByUserId(principal.getName());

        GameState gameState = gameStateRepository.getGameState(message.getGameId());
        if (!gameState.determinePlayerIds().contains(sendingPlayer.getPlayerId())) {

            gameState.fillSeat(sendingPlayer);
            gameStateRepository.updateGameState(gameState);

            updateGameStateForAllPlayers(sendingPlayer.getPlayerId(), gameState);

            List<ActiveGame> activeGames = ActiveGamesRepository.getInstance().getActiveGames();
            webSocket.convertAndSend("/topic/lobby", new ActiveGamesMessage(activeGames));
        }
    }

    @MessageMapping("/game/leave")
    public void leaveGame(GameLeaveMessage message, Principal principal) {
        Player sendingPlayer = PlayerRepository.getInstance().getPlayerByUserId(principal.getName());
        String sendingPlayerId = sendingPlayer.getPlayerId();

        GameState gameState = gameStateRepository.getGameState(message.getGameId());
        if (gameState.determinePlayerIds().contains(sendingPlayerId)) {
            gameStateRepository.removePlayerFromGames(sendingPlayer);

            updateGameStateForAllPlayers(sendingPlayerId, gameState);

            List<ActiveGame> activeGames = ActiveGamesRepository.getInstance().getActiveGames();
            webSocket.convertAndSend("/topic/lobby", new ActiveGamesMessage(activeGames));
        }
    }

    private void updateGameStateForAllPlayers(String sendingPlayerId, GameState gameState) {
        for (String playerId : gameState.determinePlayerIds()) {
            String userId = PlayerRepository.getInstance().getPlayerByPlayerId(playerId).getUserId();

            PlayerGameState playerGameState = GameStateToPlayerGameStateConverter.toPlayerGameState(playerId, gameState);
            webSocket.convertAndSendToUser(userId, "/topic/game", playerGameState);
        }
        if (!gameState.determinePlayerIds().contains(sendingPlayerId)) {
            String userId = PlayerRepository.getInstance().getPlayerByPlayerId(sendingPlayerId).getUserId();
            webSocket.convertAndSendToUser(userId, "/topic/game", PlayerGameState.playerLeftGameState(gameState.getGameId()));
        }
    }
}
var stompTopicClient = null; // for general info, like 'player plays card X'
var stompQueueClient = null; // for user-specific info, like cards in hand

function setConnected(connected) {
    $('#connect').prop('disabled', connected);
    $('#disconnect').prop('disabled', !connected);

    $('#generalInfo').html('');
}

function connectToLobby() {
    $('#lobby').show();
    $('#table').hide();

    var socket = new SockJS('/klaverjas-websocket');
    stompTopicClient = Stomp.over(socket);
    stompTopicClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompTopicClient.subscribe('/topic/lobby', handleLobbyMessage);

        stompTopicClient.send('/app/lobby/hello', {},  JSON.stringify({}));
    });
}

function connectToGameInfo() {
    var socket = new SockJS('/klaverjas-websocket');
    stompQueueClient = Stomp.over(socket);
    stompQueueClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompQueueClient.subscribe('/user/topic/game', handleGameState);
    });
}

$(function () {
    $('form').on('submit', function (e) {
        e.preventDefault();
    });

     connectToLobby();
     connectToGameInfo();

    $( '#leaveLobby' ).click(function() { leaveLobby();  });
    $( '#startGame' ).click(function() { startGame(); });
    $( '#leaveGame' ).click(function() { leaveGame(); });
    $( '#requestState' ).click(function() { requestState(); });

    $('#lobby').show();
    $('#table').hide();
});
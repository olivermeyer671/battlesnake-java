package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

import java.lang.Math;

/**
 * This is a simple Battlesnake server written in Java.
 * 
 * For instructions see
 * https://github.com/BattlesnakeOfficial/starter-snake-java/README.md
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    /**
     * Main entry point.
     *
     * @param args are ignored.
     */
    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port == null) {
            LOG.info("Using default port: {}", port);
            port = "8080";
        } else {
            LOG.info("Found system provided port: {}", port);
        }
        port(Integer.parseInt(port));
        get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        /**
         * For the start/end request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         * @param req
         * @param res
         * @return
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @return a response back to the engine containing the Battlesnake setup
         *         values.
         */
        public Map<String, String> index() {
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", ""); // TODO: Your Battlesnake Username
            response.put("color", "#FF0000"); // TODO: Personalize
            response.put("head", "ski"); // TODO: Personalize
            response.put("tail", "weight"); // TODO: Personalize
            return response;
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @param startRequest a JSON data map containing the information about the game
         *                     that is about to be played.
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            return EMPTY;
        }

        /**
         * This method is called on every turn of a game. It's how your snake decides
         * where to move.
         * 
         * Use the information in 'moveRequest' to decide your next move. The
         * 'moveRequest' variable can be interacted with as
         * com.fasterxml.jackson.databind.JsonNode, and contains all of the information
         * about the Battlesnake board for each move of the game.
         * 
         * For a full example of 'json', see
         * https://docs.battlesnake.com/references/api/sample-move-request
         *
         * @param moveRequest JsonNode of all Game Board data as received from the
         *                    Battlesnake Engine.
         * @return a Map<String,String> response back to the engine the single move to
         *         make. One of "up", "down", "left" or "right".
         */
        public Map<String, String> move(JsonNode moveRequest) {
            /*
            try {
                LOG.info("Data: {}", JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(moveRequest));
            } catch (JsonProcessingException e) {
                LOG.error("Error parsing payload", e);
            }
            */

            /*
             * Example how to retrieve data from the request payload:
             * 
             * String gameId = moveRequest.get("game").get("id").asText();
             * 
             * int height = moveRequest.get("board").get("height").asInt();
             * 
             */
            
            JsonNode head = moveRequest.get("you").get("head");
            JsonNode body = moveRequest.get("you").get("body");
            JsonNode board = moveRequest.get("board");
            int[][] gameBoard = new int[moveRequest.get("board").get("width").asInt() + 2][moveRequest.get("board").get("height").asInt() + 2];

          
            
            ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

            buildGameBoard(moveRequest, gameBoard); 
            //chooseDirection(moveRequest, gameBoard, possibleMoves);
            floodFill(moveRequest, gameBoard, possibleMoves);
    
            // Don't allow your Battlesnake to move back in on it's own neck
            //avoidMyNeck(head, body, possibleMoves);

            // TODO: Using information from 'moveRequest', find the edges of the board and
            // don't
            // let your Battlesnake move beyond them board_height = ? board_width = ?
            //avoidBorders(head, board, possibleMoves);

            // TODO Using information from 'moveRequest', don't let your Battlesnake pick a
            // move
            // that would hit its own body
            //avoidMyBody(moveRequest, possibleMoves);
            // TODO: Using information from 'moveRequest', don't let your Battlesnake pick a
            // move
            // that would collide with another Battlesnake
            //avoidOthers(moveRequest, possibleMoves);

            //avoidHazards(moveRequest, possibleMoves);
            // TODO: Using information from 'moveRequest', make your Battlesnake move
            // towards a
            // piece of food on the board
            //findFood(moveRequest, possibleMoves);
            // Choose a random direction to move in
            final int choice = new Random().nextInt(possibleMoves.size());
            final String move = possibleMoves.get(choice);

            //LOG.info("MOVE {}", move);

            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            return response;
        }

        /**
         * Remove the 'neck' direction from the list of possible moves
         * 
         * @param head          JsonNode of the head position e.g. {"x": 0, "y": 0}
         * @param body          JsonNode of x/y coordinates for every segment of a
         *                      Battlesnake. e.g. [ {"x": 0, "y": 0}, {"x": 1, "y": 0},
         *                      {"x": 2, "y": 0} ]
         * @param possibleMoves ArrayList of String. Moves to pick from.
         */
        public void avoidMyNeck(JsonNode head, JsonNode body, ArrayList<String> possibleMoves) {
            JsonNode neck = body.get(1);

            if (neck.get("x").asInt() < head.get("x").asInt()) {
                possibleMoves.remove("left");
            } else if (neck.get("x").asInt() > head.get("x").asInt()) {
                possibleMoves.remove("right");
            } else if (neck.get("y").asInt() < head.get("y").asInt()) {
                possibleMoves.remove("down");
            } else if (neck.get("y").asInt() > head.get("y").asInt()) {
                possibleMoves.remove("up");
            }
        }

        public void avoidBorders(JsonNode head, JsonNode board, ArrayList<String> possibleMoves) {
          int height = board.get("height").asInt();
          int width = board.get("width").asInt();
          int headX = head.get("x").asInt();
          int headY = head.get("y").asInt();  
                
          if (headX <= 0) {
              possibleMoves.remove("left");
          } if (headX >= width - 1) {
              possibleMoves.remove("right");
          } if (headY <= 0) {
              possibleMoves.remove("down");
          } if (headY >= height - 1) {
              possibleMoves.remove("up");
          }
        }

        public void avoidMyBody(JsonNode moveRequest, ArrayList<String> possibleMoves) {

            int headX = moveRequest.get("you").get("head").get("x").asInt();
            int headY = moveRequest.get("you").get("head").get("y").asInt();          
            JsonNode snake = moveRequest.get("you");
            
            for(int i = 0; i < snake.get("length").asInt() - 1; i++) {
              int bodyX = snake.get("body").get(i).get("x").asInt();
              int bodyY = snake.get("body").get(i).get("y").asInt();
              if ((bodyX == headX - 1) && (bodyY == headY)) {
                possibleMoves.remove("left");
              } if ((bodyX == headX + 1) && (bodyY == headY)) {
                possibleMoves.remove("right");
              } if ((bodyX == headX) && (bodyY == headY - 1)) {
                possibleMoves.remove("down");
              } if ((bodyX == headX) && (bodyY == headY + 1)) {
                possibleMoves.remove("up");
              }
              
            }
      

            
        }


        public void avoidOthers(JsonNode moveRequest, ArrayList<String> possibleMoves) {

            int headX = moveRequest.get("you").get("head").get("x").asInt();
            int headY = moveRequest.get("you").get("head").get("y").asInt();          
            

            for (int j = 0; j < moveRequest.get("board").get("snakes").size(); j++) {
              JsonNode snake = moveRequest.get("board").get("snakes").get(j);
              for(int i = 0; i < snake.get("length").asInt() - 1; i++) {
                int bodyX = snake.get("body").get(i).get("x").asInt();
                int bodyY = snake.get("body").get(i).get("y").asInt();
                if ((bodyX == headX - 1) && (bodyY == headY)) {
                  possibleMoves.remove("left");
                } if ((bodyX == headX + 1) && (bodyY == headY)) {
                  possibleMoves.remove("right");
                } if ((bodyX == headX) && (bodyY == headY - 1)) {
                  possibleMoves.remove("down");
                } if ((bodyX == headX) && (bodyY == headY + 1)) {
                  possibleMoves.remove("up");
                }
                
              }
            }      

            
        }


        public void avoidHazards(JsonNode moveRequest, ArrayList<String> possibleMoves) {

          int headX = moveRequest.get("you").get("head").get("x").asInt();
          int headY = moveRequest.get("you").get("head").get("y").asInt();          
          

          for (int i = 0; i < moveRequest.get("board").get("hazards").size(); i++) {
            JsonNode snake = moveRequest.get("board").get("hazards").get(i);
            
            int bodyX = snake.get("x").asInt();
            int bodyY = snake.get("y").asInt();
            if ((bodyX == headX - 1) && (bodyY == headY)) {
              possibleMoves.remove("left");
            } if ((bodyX == headX + 1) && (bodyY == headY)) {
              possibleMoves.remove("right");
            } if ((bodyX == headX) && (bodyY == headY - 1)) {
              possibleMoves.remove("down");
            } if ((bodyX == headX) && (bodyY == headY + 1)) {
              possibleMoves.remove("up");
            }
              
          
          }      

            
        }


        public void findFood(JsonNode moveRequest, ArrayList<String> possibleMoves) {

          int headX = moveRequest.get("you").get("head").get("x").asInt();
          int headY = moveRequest.get("you").get("head").get("y").asInt();  
          if ((moveRequest.get("board").get("food").size() > 0) && (moveRequest.get("you").get("health").asInt() < 100)) {
            int foodX = moveRequest.get("board").get("food").get(0).get("x").asInt();
            int foodY = moveRequest.get("board").get("food").get(0).get("y").asInt();
            
            for (int i = 0; i < moveRequest.get("board").get("food").size(); i++) {
              int tempX = moveRequest.get("board").get("food").get(i).get("x").asInt();
              int tempY = moveRequest.get("board").get("food").get(i).get("y").asInt();
  
              if (Math.abs(Math.abs(headX - tempX) + Math.abs(headY - tempY)) < Math.abs(Math.abs(headX - foodX) + Math.abs(headY - foodY))) {
                foodX = tempX;
                foodY = tempY;
              }  
            }      
  
            if ((foodX > headX) && (foodY == headY) && possibleMoves.contains("right")){
              possibleMoves.remove("left");
              possibleMoves.remove("up");
              possibleMoves.remove("down");
            } if ((foodX < headX) && (foodY == headY) && possibleMoves.contains("left")){
              possibleMoves.remove("right");
              possibleMoves.remove("up");
              possibleMoves.remove("down");
            } if ((foodX == headX) && (foodY > headY) && possibleMoves.contains("up")){
              possibleMoves.remove("down");
              possibleMoves.remove("left");
              possibleMoves.remove("right");
            } if ((foodX == headX) && (foodY < headY) && possibleMoves.contains("down")){
              possibleMoves.remove("up");
              possibleMoves.remove("left");
              possibleMoves.remove("right");
            } 
            
            if ((foodX > headX) && (foodY > headY) && (possibleMoves.contains("right") | possibleMoves.contains("up"))){
              possibleMoves.remove("left");
              possibleMoves.remove("down");
            } if ((foodX > headX) && (foodY < headY) && (possibleMoves.contains("right") | possibleMoves.contains("down"))){
              possibleMoves.remove("left");
              possibleMoves.remove("up");
            } if ((foodX < headX) && (foodY > headY) && (possibleMoves.contains("left") | possibleMoves.contains("up"))){
              possibleMoves.remove("right");
              possibleMoves.remove("down");
            } if ((foodX < headX) && (foodY < headY) && (possibleMoves.contains("left") | possibleMoves.contains("down"))){
              possibleMoves.remove("right");
              possibleMoves.remove("up");
            }
          }  
        }

        //create gameboard (2d array) with a border of cells to represent out of bounds
        //all locations must be shifted one up and to the right due to border
        public void buildGameBoard(JsonNode moveRequest, int[][] gameBoard) {

          int borderValue = -9;
          int hazardValue = -2;
          int snakeValue = -4;
          int foodValue = 1;
          
          int height = moveRequest.get("board").get("height").asInt();
          int width = moveRequest.get("board").get("width").asInt();

          //clear gameBoard
          for (int i=0; i<width+2; i++) {
            for (int j=0; j<height+2; j++) {
              gameBoard[i][j] = 0;
            }
          }
          
          //populate borders of gameBoard
          for (int i=0; i<width+2; i++) {
            for (int j=0; j<height+2; j++) {
              if ((i==0)|(i==width+1)|(j==0)|(j==height+1)) {
                gameBoard[i][j] = borderValue;
              }
            }
          }

          //populate gameBoard with food  
          for (int i = 0; i < moveRequest.get("board").get("food").size(); i++) {
              int foodX = moveRequest.get("board").get("food").get(i).get("x").asInt();
              int foodY = moveRequest.get("board").get("food").get(i).get("y").asInt();
              gameBoard[foodX+1][foodY+1] = foodValue;
          }

          //populate gameBoard with hazards
          for (int i = 0; i < moveRequest.get("board").get("hazards").size(); i++) {
            JsonNode hazard = moveRequest.get("board").get("hazards").get(i);
            int hazardX = hazard.get("x").asInt();
            int hazardY = hazard.get("y").asInt();
            gameBoard[hazardX+1][hazardY+1] = hazardValue;
          }  

          //populate gameBoard with snakes
          for (int i = 0; i < moveRequest.get("board").get("snakes").size(); i++) {
            JsonNode snake = moveRequest.get("board").get("snakes").get(i);
            for(int j = 0; j < snake.get("length").asInt() - 1; j++) {
              int bodyX = snake.get("body").get(j).get("x").asInt();
              int bodyY = snake.get("body").get(j).get("y").asInt();
              gameBoard[bodyX+1][bodyY+1] = snakeValue;
            }
          } 

          /*
          //print gameBoard
          for (int i=0; i<width+2; i++) {
            for (int j=0; j<height+2; j++) {
              System.out.printf("'%-2d' ", gameBoard[i][j]);
            }
            System.out.printf("\n");
          }
          */
          
        }

        //choose the best direction of travel based on immediate neighbours to the head of your snake
        //cannot avoid trapping itself in a hole and does not search for distant food yet
        //tends to travel in straight lines when all options are equal
        public void chooseDirection(JsonNode moveRequest, int[][] gameBoard, ArrayList<String> possibleMoves) {
          possibleMoves.clear();
          int headX = moveRequest.get("you").get("head").get("x").asInt() + 1;
          int headY = moveRequest.get("you").get("head").get("y").asInt() + 1;
          int up = gameBoard[headX][headY+1];
          int down = gameBoard[headX][headY-1];
          int left = gameBoard[headX-1][headY];
          int right = gameBoard[headX+1][headY];
          int[] options = new int[4];
          options[0] = up; 
          options[1] = down;      
          options[2] = left;      
          options[3] = right;
          int max = options[0];
          int index = 0;
          String[] directions = {"up","down","left","right"};
          for (int i=0; i<4; i++) {
            if (options[i] >= 0) {
              max = options[i];
              index = i;
              possibleMoves.add(directions[i]);
            }
          }

        }

        //uses helper function to find the best path
        public void floodFill(JsonNode moveRequest, int[][] gameBoard, ArrayList<String> possibleMoves) {
          possibleMoves.clear();
          int headX = moveRequest.get("you").get("head").get("x").asInt() + 1;
          int headY = moveRequest.get("you").get("head").get("y").asInt() + 1;
          int[][] visited = new int[moveRequest.get("board").get("width").asInt() + 2][moveRequest.get("board").get("height").asInt() + 2];
          
          int up = floodFillHelper(moveRequest, gameBoard, visited, headX, headY+1, 0);
          int down = floodFillHelper(moveRequest, gameBoard, visited, headX, headY-1, 0);
          int left = floodFillHelper(moveRequest, gameBoard, visited, headX-1, headY, 0);
          int right = floodFillHelper(moveRequest, gameBoard, visited, headX+1, headY, 0);

          int[] options = new int[4];
          options[0] = up; 
          options[1] = down;      
          options[2] = left;      
          options[3] = right;
          int max = options[0];
          int index = 0;
          for (int i=0; i<4; i++) {
            if (options[i] >= max) {
              max = options[i];
              index = i;
            }
          } 
          String[] directions = {"up","down","left","right"};
          for (int i=0; i<4; i++) {
            if (options[i] >= moveRequest.get("you").get("length").asInt()) {
              possibleMoves.add(directions[i]);
            }
          } 

          if ((moveRequest.get("you").get("health").asInt() < 20) && (moveRequest.get("board").get("food").size() > 0)) {
            possibleMoves.clear();
            possibleMoves.add("up");
            possibleMoves.add("down");
            possibleMoves.add("left");
            possibleMoves.add("right");
            avoidBorders(moveRequest.get("you").get("head"), moveRequest.get("board"), possibleMoves);
            avoidOthers(moveRequest, possibleMoves);
            avoidHazards(moveRequest, possibleMoves);
            findFood(moveRequest, possibleMoves);
            
          }

          /*
          if (headX+1 == moveRequest.get("board").get("width").asInt() + 1) {
            if (possibleMoves.contains("down")|possibleMoves.contains("left")|possibleMoves.contains("up")) {
              possibleMoves.remove("right");
            }
          }
          if (headX-1 == 1) {
            if (possibleMoves.contains("down")|possibleMoves.contains("right")|possibleMoves.contains("up")) {
              possibleMoves.remove("left");
            }
          }
          if (headY+1 == moveRequest.get("board").get("height").asInt() + 1) {
            if (possibleMoves.contains("down")|possibleMoves.contains("left")|possibleMoves.contains("right")) {
              possibleMoves.remove("up");
            }
          }
          if (headY-1 == 1) {
            if (possibleMoves.contains("up")|possibleMoves.contains("left")|possibleMoves.contains("right")) {
              possibleMoves.remove("down");
            }
          }
          */
        }

        //recursive flood fill algorithm to help the snake avoid trapping itself in its body
        private int floodFillHelper(JsonNode moveRequest, int[][] gameBoard, int[][] visited, int x, int y, int depth) {
          int value = 1;
          if (depth < (moveRequest.get("you").get("length").asInt())*2 + 2) {  
            if (gameBoard[x][y] >= 0) {
              if ((gameBoard[x][y] == 1)&&(moveRequest.get("you").get("health").asInt() < 20)) {
                value = 4;
              }
              if (visited[x][y] == 0) {
                visited[x][y] = 1;
                return value + floodFillHelper(moveRequest, gameBoard, visited, x, y+1, depth+1) + floodFillHelper(moveRequest, gameBoard, visited, x, y-1, depth+1) + floodFillHelper(moveRequest, gameBoard, visited, x-1, y, depth+1) + floodFillHelper(moveRequest, gameBoard, visited, x, y, depth+1);
              }
            }
          }
          return 0;
        }
      
      
        /**
         * This method is called when a game your Battlesnake was in ends.
         * 
         * It is purely for informational purposes, you don't have to make any decisions
         * here.
         *
         * @param endRequest a map containing the JSON sent to this snake. Use this data
         *                   to know which game has ended
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> end(JsonNode endRequest) {
            LOG.info("END");
            return EMPTY;
        }
    }

}

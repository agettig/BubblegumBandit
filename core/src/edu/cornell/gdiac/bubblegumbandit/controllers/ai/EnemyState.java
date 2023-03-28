/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package edu.cornell.gdiac.bubblegumbandit.controllers.ai;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import edu.cornell.gdiac.bubblegumbandit.controllers.AIController;
import edu.cornell.gdiac.bubblegumbandit.controllers.fsm.MessageType;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;
import static edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph.GRAVITY_DOWN_TILE;
import static edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph.JUMP_TILE;

public enum EnemyState implements State<EnemyController> {

    // Initial state of all npcs
    SPAWN() {
        @Override
        public void enter (EnemyController  aiController) {
            // if the miner is not already located at the goldmine, he must
            // change location to the gold mine
            talk(aiController, "Enter spawn");
        }

        @Override
        public void update (EnemyController  aiController) {
            // Now EnemyModel is at the goldmine he digs for gold until he
            // is carrying in excess of MAX_NUGGETS. If he gets thirsty during
            // his digging he packs up work for a while and changes state to
            // go to the saloon for a whiskey.
            if (aiController.getEnemyStateMachine().getTicks() > 120){
                aiController.getEnemyStateMachine().changeState(WANDER);
            }
            aiController.getEnemy().setNextAction(CONTROL_NO_ACTION);
        }
        @Override
        public void exit (EnemyController aiController) {
            talk(aiController, "Leave spawn");
        }
    },

    WANDER() {
        public void enter (EnemyController aiController) {
            talk(aiController, "enter wander");
        }

        @Override
        public void update (EnemyController aiController) {
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();
            if(enemy.vision.canSee(bandit)){
                aiController.getEnemyStateMachine().changeState(CHASE);
            }
            setAction(aiController);
        }

        public void setAction(EnemyController aiController){
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();

            // if player comes within sensing ray cast
            // the enemy will turn around
            if (enemy.getSensing().canSee(bandit)){
                boolean facingRight = enemy.getFaceRight();
                enemy.setFaceRight(!facingRight);
                enemy.setNextAction(facingRight ? CONTROL_MOVE_LEFT : CONTROL_MOVE_RIGHT);
                return;
            }

            if (aiController.getTileType() == JUMP_TILE || enemy.isJumping()){
                enemy.setNextAction(CONTROL_JUMP);
                enemy.setIsJumping(true);
                return;
            }

            if(aiController.getTileType() == GRAVITY_DOWN_TILE) enemy.setIsJumping(false);

            int moveRight;
            int moveLeft;
            int action = CONTROL_NO_ACTION;

            moveLeft = aiController.getEnemyStateMachine().getNextMove((int)enemy.getX() - 1, (int) enemy.getY());

            moveRight = aiController.getEnemyStateMachine().getNextMove((int)enemy.getX() + 1, (int) enemy.getY());

            if (enemy.getFaceRight()){
                if ( moveRight != CONTROL_NO_ACTION){
                    action = moveRight;
                }
                else{
                    action = moveLeft;
                }
            }
            else{
                if ( moveLeft != CONTROL_NO_ACTION){
                    action = moveLeft;
                }
                else{
                    action = moveRight;
                }
            }
            enemy.setNextAction(action);
        }

        @Override
        public void exit (EnemyController aiController) {
            talk(aiController, "leave wander");
        }

        @Override
        public boolean onMessage (EnemyController aiController, Telegram telegram) {
//            if (telegram.message == MessageType.STEW_READY) {
//
//                talk(EnemyModel, "Message STEW_READY handled at time: " + GdxAI.getTimepiece().getTime());
//
//                talk(EnemyModel, "Okay Hun, ahm a comin'!");
//
//                EnemyModel.getStateMachine().changeState(EAT_STEW);
//
//                return true;
//            }
//
            return false; // send message to global message handler
        }
    },

     CHASE() {
        @Override
        public void enter (EnemyController aiController) {
            talk(aiController, "enter chase");
        }

        @Override
        public void update (EnemyController aiController) {
            setAction(aiController);
            if(aiController.getEnemy().getAttacking().canSee(aiController.getBandit())){
                aiController.getEnemyStateMachine().changeState(ATTACK);
            }
        }

        private void setAction(EnemyController aiController){
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;
            if (!aiController.getEnemyStateMachine().canMove()){

            }
            else{
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }
            if (move == CONTROL_NO_ACTION){
                aiController.getEnemyStateMachine().changeState(WANDER);
            }
            aiController.getEnemy().setNextAction(move);
        }

        @Override
        public void exit (EnemyController aiController) {
            talk(aiController, "leave chase");
        }
    },

    ATTACK() {
        @Override
        public void enter (EnemyController aiController) {
            talk(aiController, "enter attack");
        }

        @Override
        public void update (EnemyController aiController) {
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;
            if (!aiController.getEnemyStateMachine().canMove()){

            }
            else{
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }
            if (aiController.canShootTarget()){
                move = move | CONTROL_FIRE;
            }
            if (move == CONTROL_NO_ACTION){
                aiController.getEnemyStateMachine().changeState(WANDER);
            }
            aiController.getEnemy().setNextAction(move);
        }

        @Override
        public void exit (EnemyController aiController) {
            talk(aiController, "leave attack");
        }
    },

    PERCEIVE(){
        @Override
        public void enter(EnemyController aiController){
            talk(aiController, "enter perceive");
        };

        @Override
        public void update(EnemyController aiController){
            if (aiController.getBandit().isOrbCollected()){
                aiController.getEnemyStateMachine().changeState(PURSUE);
            }
        };
        @Override
        public void exit(EnemyController aiController){
            talk(aiController, "leave perceive");
        };


    },

    STUCK(){
        @Override
        public void enter(EnemyController aiController){
            talk(aiController, "enter stuck");
        };

        @Override
        public void update(EnemyController aiController){
            talk(aiController, "in stuck");
        };
        @Override
        public void exit(EnemyController aiController){
            talk(aiController, "leave stuck");
        };

    },

    RETREAT(){
        @Override
        public void enter(EnemyController aiController){
            talk(aiController, "enter retreat");
        };

        @Override
        public void update(EnemyController aiController){
            talk(aiController, "in retreat");
        };
        @Override
        public void exit(EnemyController aiController){
            talk(aiController, "leave retreat");
        };

    },

    PURSUE(){
        @Override
        public void enter(EnemyController aiController){
            // set more agressive setting, smaller cooldown and faster speed
            talk(aiController, "enter pursue");
        };

        @Override
        public void update(EnemyController aiController){
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;
            if (!aiController.getEnemyStateMachine().canMove()){

            }
            else{
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }
            if (aiController.canShootTarget()){
                move = move | CONTROL_FIRE;
            }
            if (move == CONTROL_NO_ACTION){
                aiController.getEnemyStateMachine().changeState(WANDER);
            }
            aiController.getEnemy().setNextAction(move);
        };
        @Override
        public void exit(EnemyController aiController){
            talk(aiController, "leave pursue");
        };
    };

    @Override
    public boolean onMessage (EnemyController aiController, Telegram telegram) {


        return false;
    }

    protected void talk (EnemyController aiController, String msg) {
        GdxAI.getLogger().info(aiController.getEnemy().getName(), msg);
    }


}

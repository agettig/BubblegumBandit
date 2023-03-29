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
import com.badlogic.gdx.ai.msg.Telegram;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import static edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel.*;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;
import static edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph.GRAVITY_DOWN_TILE;
import static edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph.JUMP_TILE;

public enum EnemyState implements State<EnemyController> {

    // Initial state of all npcs
    SPAWN() {
        @Override
        public void enter (EnemyController  aiController) {
            talk(aiController, "Enter spawn");
        }

        @Override
        public void update (EnemyController  aiController) {
            // change to wander after 120 ticks
            if (aiController.getEnemyStateMachine().getTicks() > 120){
                aiController.getEnemyStateMachine().changeState(WANDER);
            }
            // set next action to no action
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
            aiController.getEnemy().changeSpeed(WANDER_SPEED);
        }

        @Override
        public void update (EnemyController aiController) {
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();
            // change to chase if enemy can see or hear bandit
            if(enemy.vision.canSee(bandit) || aiController.enemyHeardBandit()){
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

            int moveRight;
            int moveLeft;
            int action = CONTROL_NO_ACTION;

            // action from moving to the left
            moveLeft = aiController.getEnemyStateMachine().getNextMove((int)enemy.getX() - 1, (int) enemy.getY());

            // action from moving to the right
            moveRight = aiController.getEnemyStateMachine().getNextMove((int)enemy.getX() + 1, (int) enemy.getY());

            // if enemy can move right and is facing right move right
            // if enemy can move left and is facing left move left
            // try other direction if can't move in direction the enemy is facing
            // else don't move
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

            return false; // send message to global message handler
        }
    },

     CHASE() {
        @Override
        public void enter (EnemyController aiController) {
            talk(aiController, "enter chase");
            aiController.getEnemy().changeSpeed(CHASE_SPEED);
        }

        @Override
        public void update (EnemyController aiController) {

            // enter attack if player is in attack range
            if(aiController.getEnemy().getAttacking().canSee(aiController.getBandit())){
                aiController.getEnemyStateMachine().changeState(ATTACK);
            }
            // enter wander if enemy is out of hearing range
            if (!aiController.enemyHeardBandit()) {
                aiController.getEnemyStateMachine().changeState(WANDER);
            }
            // update action
            setAction(aiController);
        }

        private void setAction(EnemyController aiController){
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;

            // get next move if enemy is on a board path
            if (aiController.getEnemyStateMachine().canMove()){
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }

            // set next action
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
            aiController.getEnemy().changeSpeed(CHASE_SPEED);
        }

        @Override
        public void update (EnemyController aiController) {
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;

            // set state to wander if enemy can not hear bandit
            if (!aiController.enemyHeardBandit()) {
                aiController.getEnemyStateMachine().changeState(WANDER);
            }

            // if can move find next move
            if (aiController.getEnemyStateMachine().canMove()){
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }

            // shoot player
            if (aiController.canShootTarget()){
                move = move | CONTROL_FIRE;
            }

            // set next action
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
            // if orb is collected and enemy is not stuck, change state to pursue
            if (aiController.getBandit().isOrbCollected() && !aiController.getEnemyStateMachine().isInState(STUCK)){
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
            // if player comes within sensing ray cast
            // the enemy will turn around
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();
            if (enemy.getSensing().canSee(bandit)){
                boolean facingRight = enemy.getFaceRight();
                enemy.setFaceRight(!facingRight);
                enemy.setNextAction(facingRight ? CONTROL_MOVE_LEFT : CONTROL_MOVE_RIGHT);
            }
            int move = CONTROL_NO_ACTION;
            if (aiController.canShootTarget()){
                move = move | CONTROL_FIRE;
            }
            aiController.getEnemy().setNextAction(move);
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
            aiController.getEnemy().changeSpeed(PURSUE_SPEED);
        };

        @Override
        public void update(EnemyController aiController){
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;

            // get next move
            if (aiController.getEnemyStateMachine().canMove()){
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }

            // determine if enemy can shoot
            if (aiController.canShootTarget()){
                move = move | CONTROL_FIRE;
            }

            // set next move
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

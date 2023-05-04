package edu.cornell.gdiac.bubblegumbandit.controllers.ai;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.helpers.Shield;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.*;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;

import static edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel.*;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;

public enum EnemyState implements State<AIController> {

    SPAWN() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "Enter spawn");
        }

        @Override
        public void update(AIController aiController) {
            // change to wander after 120 ticks
            if (aiController.getEnemyStateMachine().getTicks() > 120) {
                aiController.getEnemyStateMachine().changeState(WANDER);
            }

            aiController.getEnemy().isShielded(true);

            // set next action to no action
            aiController.getEnemy().setNextAction(CONTROL_NO_ACTION);
        }

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "Leave spawn");
        }
    },

    WANDER() {
        public void enter(AIController aiController) {
            talk(aiController, "enter wander");
            aiController.getEnemy().changeSpeed(WANDER_SPEED);
        }

        @Override
        public void update(AIController aiController) {
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();
            // change to chase if enemy can see or hear bandit
            if (enemy.vision.canSee(bandit) || aiController.enemyHeardBandit()) {
                aiController.getEnemyStateMachine().changeState(CHASE);
            }
            setAction(aiController);

            aiController.getEnemy().isShielded(true);
            if (aiController.getEnemy() instanceof LaserEnemyModel) {
                if (!((LaserEnemyModel) aiController.getEnemy()).inactiveLaser()) {
                    aiController.getEnemy().isShielded(false);
                }
            }

        }

        public void setAction(AIController aiController) {
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();

            // if player comes within sensing ray cast
            // the enemy will turn around
            if (enemy.getSensing().canSee(bandit)) {
                boolean facingRight = enemy.getFaceRight();
                enemy.setFaceRight(!facingRight);
                enemy.setNextAction(facingRight ? CONTROL_MOVE_LEFT : CONTROL_MOVE_RIGHT);
                return;
            }

            int moveRight;
            int moveLeft;
            int action = CONTROL_NO_ACTION;


            // action from moving to the left
            moveLeft = aiController.getEnemyStateMachine().getNextMove((int) enemy.getX() - 1, (int) enemy.getYFeet());

            // action from moving to the right
            moveRight = aiController.getEnemyStateMachine().getNextMove((int) enemy.getX() + 1, (int) enemy.getYFeet());

            // if enemy can move right and is facing right move right
            // if enemy can move left and is facing left move left
            // try other direction if can't move in direction the enemy is facing
            // else don't move
            if (enemy.getFaceRight()) {
                if (moveRight != CONTROL_NO_ACTION) {
                    action = moveRight;
                } else {
                    action = moveLeft;
                }
            } else {
                if (moveLeft != CONTROL_NO_ACTION) {
                    action = moveLeft;
                } else {
                    action = moveRight;
                }
            }
            enemy.setNextAction(action);
        }

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave wander");
        }

    },

    CHASE() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter chase");
            aiController.getEnemy().changeSpeed(CHASE_SPEED);
        }

        @Override
        public void update(AIController aiController) {

            // enter attack if player is in attack range
            if (aiController.getEnemy().getAttacking().canSee(aiController.getBandit())) {
                aiController.getEnemyStateMachine().changeState(ATTACK);
            }
            // enter wander if enemy is out of hearing range
            if (!aiController.enemyHeardBandit()) {
                aiController.getEnemyStateMachine().changeState(WANDER);
            }

            aiController.getEnemy().isShielded(true);
            if (aiController.getEnemy() instanceof LaserEnemyModel) {
                if (!((LaserEnemyModel) aiController.getEnemy()).inactiveLaser()) {
                    aiController.getEnemy().isShielded(false);
                }
            }


            // update action
            setAction(aiController);
        }

        private void setAction(AIController aiController) {
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;

            // get next move if enemy is on a board path
            if (aiController.getEnemyStateMachine().canMove() && (aiController.getEnemy() instanceof RollingEnemyModel || !aiController.enemyCloseToBandit())) {
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());

                // flip towards direction of bandit if enemy can not move
                if (move == 0) {

                    if (banditModel.getX() < aiController.getEnemy().getX()) {
                        aiController.getEnemy().setFaceRight(false);
                    } else {
                        aiController.getEnemy().setFaceRight(true);
                    }
                }
            }


            // set next action
            aiController.getEnemy().setNextAction(move);
        }

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave chase");
        }
    },

    ATTACK() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter attack");
            aiController.getEnemy().changeSpeed(CHASE_SPEED);
        }

        @Override
        public void update(AIController aiController) {
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;

            boolean isRollingEnemy = aiController.enemy instanceof RollingEnemyModel;

            // set state to wander if enemy can not hear bandit
            if (!aiController.enemyHeardBandit() && (!(isRollingEnemy && ((RollingEnemyModel) aiController.enemy).isRolling()) || !isRollingEnemy)) {
                aiController.getEnemyStateMachine().changeState(WANDER);
            }

            aiController.getEnemy().isShielded(false);

            // if can move find next move
            if (aiController.getEnemyStateMachine().canMove() && (aiController.getEnemy() instanceof RollingEnemyModel || !aiController.enemyCloseToBandit())) {
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }

//            if (move == CONTROL_NO_ACTION && (aiController.getEnemy() instanceof RollingEnemyModel )){
//                aiController.getEnemyStateMachine().changeState(WANDER);
//            }
            // shoot player
            if (aiController.canShootTarget() ||(isRollingEnemy && ((RollingEnemyModel) aiController.enemy).isRolling())) {
                move = move | CONTROL_FIRE;
            }

            // set next action
            aiController.getEnemy().setNextAction(move);
        }

        @Override
        public void exit(AIController aiController) {
            if (aiController.getEnemy() instanceof RollingEnemyModel) {
                ((RollingEnemyModel) aiController.getEnemy()).resetAttack();
            }
            talk(aiController, "leave attack");
        }
    },

    PERCEIVE() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter perceive");
        }

        ;

        @Override
        public void update(AIController aiController) {
            // if orb is collected and enemy is not stuck, change state to pursue
            if (aiController.getBandit().isOrbCollected() && !aiController.getEnemyStateMachine().isInState(STUCK) && !aiController.getEnemyStateMachine().isInState(PURSUE)) {
                aiController.getEnemyStateMachine().changeState(PURSUE);
            }
        }

        ;

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave perceive");
        }

        ;


    },

    STUCK() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter stuck");
        }

        ;

        @Override
        public void update(AIController aiController) {
            // if player comes within sensing ray cast
            // the enemy will turn around
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();
            if (enemy.getSensing().canSee(bandit)) {
                boolean facingRight = enemy.getFaceRight();
                enemy.setFaceRight(!facingRight);
                enemy.setNextAction(facingRight ? CONTROL_MOVE_LEFT : CONTROL_MOVE_RIGHT);
            }
            int move = CONTROL_NO_ACTION;
            if (aiController.canShootTarget()) {
                move = move | CONTROL_FIRE;
            }
            aiController.getEnemy().setNextAction(move);

            if (aiController.getEnemyStateMachine().getTicks() % 200 == 0) {
                aiController.getEnemyStateMachine().broadcastMessage(MessageType.NEED_BACKUP, aiController.getEnemy().getPosition());
            }

        }

        ;

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave stuck");
        }

        ;

    },

    RETREAT() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter retreat");
        }

        ;

        @Override
        public void update(AIController aiController) {
            talk(aiController, "in retreat");
        }

        ;

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave retreat");
        }

    },

    PURSUE() {
        @Override
        public void enter(AIController aiController) {
            // set more agressive setting, smaller cooldown and faster speed
            talk(aiController, "enter pursue");
            aiController.getEnemy().changeSpeed(PURSUE_SPEED);
        }

        ;

        @Override
        public void update(AIController aiController) {
            BanditModel banditModel = aiController.getBandit();
            int move = CONTROL_NO_ACTION;

            // get next move
            if (aiController.getEnemyStateMachine().canMove() && (aiController.getEnemy() instanceof RollingEnemyModel || !aiController.enemyCloseToBandit())) {
                move = aiController.getEnemyStateMachine().getNextMove(
                        (int) banditModel.getX(),
                        (int) banditModel.getY());
            }

            aiController.getEnemy().isShielded(false);

            // determine if enemy can shoot
            if (aiController.canShootTarget()) {
                move = move | CONTROL_FIRE;
            }

            // set next move
            aiController.getEnemy().setNextAction(move);
        }

        ;

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave pursue");
        }

        ;
    },

    HELPING() {
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter helping");
            aiController.getEnemy().changeSpeed(CHASE_SPEED);
        }

        @Override
        public void update(AIController aiController) {

            if (aiController.getEnemy().vision.canSee(aiController.getBandit()) || aiController.enemyHeardBandit()) {
                aiController.getEnemyStateMachine().changeState(CHASE);
                return;
            }

            setAction(aiController);
        }

        public void setAction(AIController aiController) {
            Vector2 target = aiController.getEnemy().getHelpingTarget();

            // get move to enemy in needed
            int move = aiController.getEnemyStateMachine().getNextMove((int) target.x, (int) target.y);
            if (move != CONTROL_NO_ACTION) {
                aiController.getEnemy().setNextAction(move);
                return;
            }

            // if can not reach enemy in need, try moving closer to them
            float diffX = aiController.getEnemy().getX() - target.x;
            float diffY = aiController.getEnemy().getYFeet() - target.y;

            // try moving left/right
            // move left
            if (diffX > 0) {
                move = aiController.getEnemyStateMachine().getNextMove((int) aiController.getEnemy().getX() - 1, (int) aiController.getEnemy().getYFeet());
                if (move != CONTROL_NO_ACTION) {
                    aiController.getEnemy().setNextAction(move);
                    return;
                }
            } else {
                move = aiController.getEnemyStateMachine().getNextMove((int) aiController.getEnemy().getX() + 1, (int) aiController.getEnemy().getYFeet());
                if (move != CONTROL_NO_ACTION) {
                    aiController.getEnemy().setNextAction(move);
                    return;
                }
            }

            // try moving up/down
            if (diffY > 0) {
                move = aiController.getEnemyStateMachine().getNextMove((int) aiController.getEnemy().getX(), (int) aiController.getEnemy().getYFeet() - 1);
                if (move != CONTROL_NO_ACTION) {
                    aiController.getEnemy().setNextAction(move);
                }
            } else {
                move = aiController.getEnemyStateMachine().getNextMove((int) aiController.getEnemy().getX(), (int) aiController.getEnemy().getYFeet() + 1);
                if (move != CONTROL_NO_ACTION) {
                    aiController.getEnemy().setNextAction(move);
                }
            }
        }


        @Override
        public void exit(AIController aiController) {
            talk(aiController, "leave helping");
            aiController.getEnemy().setHelpingTarget(null);
        }

        @Override
        public boolean onMessage(AIController aiController, Telegram telegram) {

            return false;
        }

    },

    GUARD() {
        // guard enemies do not move, they will only shoot if bandit is nearby
        @Override
        public void enter(AIController aiController) {
            talk(aiController, "enter guard");
        }

        @Override
        public void update(AIController aiController) {
            EnemyModel enemy = aiController.getEnemy();
            BanditModel bandit = aiController.getBandit();
            if (enemy.getSensing().canSee(bandit)) {
                boolean facingRight = enemy.getFaceRight();
                enemy.setFaceRight(!facingRight);
            }

            // shoot player
            aiController.getEnemy().setNextAction(aiController.canShootTarget() ? CONTROL_FIRE : CONTROL_NO_ACTION);
        }

        @Override
        public void exit(AIController aiController) {
            talk(aiController, "exit guard");
        }
    };

    @Override
    public boolean onMessage(AIController aiController, Telegram telegram) {

        // if enemy is in wander and help message received
        if (aiController.getEnemyStateMachine().isInState(WANDER) &&
                telegram.message == MessageType.NEED_BACKUP) {
            aiController.getEnemyStateMachine().changeState(HELPING);
            Vector2 location = (Vector2) telegram.extraInfo;
            aiController.getEnemy().setHelpingTarget(location);
        }
        return true;
    }

    protected void talk(AIController aiController, String msg) {
        GdxAI.getLogger().info(aiController.getEnemy().getName(), msg);
    }
}

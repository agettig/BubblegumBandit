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
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.MovingEnemyModel;

public enum EnemyState implements State<MovingEnemyModel> {

    SPAWN() {
        @Override
        public void enter (MovingEnemyModel enemy) {
            // if the miner is not already located at the goldmine, he must
            // change location to the gold mine
            talk(enemy, "Enter spawn");
        }

        @Override
        public void update (MovingEnemyModel enemy) {
            // Now EnemyModel is at the goldmine he digs for gold until he
            // is carrying in excess of MAX_NUGGETS. If he gets thirsty during
            // his digging he packs up work for a while and changes state to
            // go to the saloon for a whiskey.
            talk(enemy, "In spawn");
            enemy.getEnemyStateMachine().changeState(WANDER);

        }
        @Override
        public void exit (MovingEnemyModel enemy) {
            talk(enemy, "Leave spawn");
        }
    },

    WANDER() {
        public void enter (MovingEnemyModel enemy) {
//            if (EnemyModel.getLocation() != Location.SHACK) {
//                talk(EnemyModel, "Walkin' home");
//                EnemyModel.setLocation(Location.SHACK);
//
//                // Let Elsa know I'm home
//                MessageManager.getInstance().dispatchMessage( //
//                        0.0f, // time delay
//                        EnemyModel, // ID of sender
//                        EnemyModel.elsa, // ID of recipient
//                        MessageType.HI_HONEY_I_M_HOME, // the message
//                        null);
//            }
        }

        @Override
        public void update (MovingEnemyModel enemy) {
//            // if miner is not fatigued start to dig for nuggets again.
//            if (!EnemyModel.isFatigued()) {
//                talk(EnemyModel, "All mah fatigue has drained away. Time to find more gold!");
//
//                EnemyModel.getStateMachine().changeState(ENTER_MINE_AND_DIG_FOR_NUGGET);
//            } else {
//                // sleep
//                EnemyModel.decreaseFatigue();
//                talk(EnemyModel, "ZZZZ... ");
//            }
            talk(enemy, "In Wander");
            enemy.getEnemyStateMachine().changeState(CHASE);
        }

        @Override
        public void exit (MovingEnemyModel enemy) {
        }

        @Override
        public boolean onMessage (MovingEnemyModel enemy, Telegram telegram) {
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
        public void enter (MovingEnemyModel enemy) {
//            if (EnemyModel.getLocation() != Location.SALOON) {
//                EnemyModel.setLocation(Location.SALOON);
//
//                talk(EnemyModel, "Boy, ah sure is thusty! Walking to the saloon");
//            }
        }

        @Override
        public void update (MovingEnemyModel enemy) {
//            EnemyModel.buyAndDrinkAWhiskey();
//
//            talk(EnemyModel, "That's mighty fine sippin liquer");
//
//            EnemyModel.getStateMachine().changeState(ENTER_MINE_AND_DIG_FOR_NUGGET);
            talk(enemy, "In Chase");
            enemy.getEnemyStateMachine().changeState(ATTACK);
        }

        @Override
        public void exit (MovingEnemyModel enemy) {
            talk(enemy, "Leaving the saloon, feelin' good");
        }
    },

    ATTACK() {
        @Override
        public void enter (MovingEnemyModel enemy) {
//            // On entry EnemyModel makes sure he is located at the bank
//            if (EnemyModel.getLocation() != Location.BANK) {
//                talk(EnemyModel, "Goin' to the bank. Yes siree");
//
//                EnemyModel.setLocation(Location.BANK);
//            }
        }

        @Override
        public void update (MovingEnemyModel enemy) {
//            // Deposit the gold
//            EnemyModel.addToWealth(EnemyModel.getGoldCarried());
//
//            EnemyModel.setGoldCarried(0);
//
//            talk(EnemyModel, "Depositing gold. Total savings now: " + EnemyModel.getWealth());
//
//            // Wealthy enough to have a well earned rest?
//            if (EnemyModel.getWealth() >= EnemyModel.COMFORT_LEVEL) {
//                talk(EnemyModel, "WooHoo! Rich enough for now. Back home to mah li'lle lady");
//
//                EnemyModel.getStateMachine().changeState(GO_HOME_AND_SLEEP_TILL_RESTED);
//            } else { // otherwise get more gold
//                EnemyModel.getStateMachine().changeState(ENTER_MINE_AND_DIG_FOR_NUGGET);
//            }
            talk(enemy, "In attack");
            enemy.getEnemyStateMachine().changeState(WANDER);
        }

        @Override
        public void exit (MovingEnemyModel enemy) {
//            talk(EnemyModel, "Leavin' the bank");
        }
    };

    @Override
    public boolean onMessage (MovingEnemyModel enemy, Telegram telegram) {
        return false;
    }

    protected void talk (MovingEnemyModel enemy, String msg) {
        GdxAI.getLogger().info(enemy.getName(), msg);
    }

}

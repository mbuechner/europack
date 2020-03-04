/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ddb.labs.europack.gui.helper;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;
import java.io.File;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 * @param <E>
 */
public class RollOncePerSessionTriggeringPolicy<E> extends TriggeringPolicyBase<E> {
    private static boolean doRolling = true;

    @Override
    public boolean isTriggeringEvent(File activeFile, E event) {
        if (doRolling) {
            doRolling = false;
            return true;
        }
        return false;
    }
}





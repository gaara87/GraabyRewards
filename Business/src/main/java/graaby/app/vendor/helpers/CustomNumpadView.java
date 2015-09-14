/*     
     Copyright (C) 2012 - Akash Ramani

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
 */

package graaby.app.vendor.helpers;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import junit.framework.Assert;

import graaby.app.vendor.R;

public class CustomNumpadView extends KeyboardView {

    CustomOnKeyboardActionListener keyListener;
    Keyboard kb = null;
    InputMethodManager imm = null;

    public CustomNumpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        kb = new Keyboard(context, R.xml.keyboard);
    }

    public void setActionListenerActivity(Activity act) {
        Assert.assertNotNull(act);
        if (keyListener == null) {
            keyListener = new CustomOnKeyboardActionListener(act);
            this.setOnKeyboardActionListener(keyListener);
            this.setKeyboard(kb);
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    ;

    private class CustomOnKeyboardActionListener implements
            OnKeyboardActionListener {
        Activity owner;

        public CustomOnKeyboardActionListener(Activity activity) {
            owner = activity;
        }

        public void onKey(int primaryCode, int[] keyCodes) {

            long eventTime = System.currentTimeMillis();
            KeyEvent event = new KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD
                            | KeyEvent.FLAG_KEEP_TOUCH_MODE);
            owner.dispatchKeyEvent(event);
        }


        public void onPress(int primaryCode) {
        }

        public void onRelease(int primaryCode) {
        }

        public void onText(CharSequence text) {
        }

        public void swipeDown() {
        }

        public void swipeLeft() {
        }

        public void swipeRight() {
        }

        public void swipeUp() {
        }

    }

}

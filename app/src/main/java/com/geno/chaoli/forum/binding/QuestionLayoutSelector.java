package com.geno.chaoli.forum.binding;

import com.geno.chaoli.forum.R;
import com.geno.chaoli.forum.model.BusinessQuestion;
import com.geno.chaoli.forum.model.Question;

/**
 * Created by jianhao on 16-10-13.
 */

public class QuestionLayoutSelector extends LayoutSelector<BusinessQuestion> {
    private final int CHECK_BTN_ITEM_TYPE = 0;
    private final int RADIO_BTN_ITEM_TYPE = 1;
    private final int EDIT_TEXT_ITEM_TYPE = 2;

    @Override
    int getLayout(int type) {
        switch (type) {
            case EDIT_TEXT_ITEM_TYPE:
                return R.layout.question_item_et;
            case CHECK_BTN_ITEM_TYPE:
                return R.layout.question_item_cb;
            case RADIO_BTN_ITEM_TYPE:
                return R.layout.question_item_rb;
            default:
                throw new IllegalArgumentException("Wrong type");
        }
    }

    @Override
    int getType(BusinessQuestion item) {
        if((item.choice.equals("false"))) return EDIT_TEXT_ITEM_TYPE;
        return item.multiAnswer ? CHECK_BTN_ITEM_TYPE : RADIO_BTN_ITEM_TYPE;
    }
}

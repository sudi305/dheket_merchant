package com.bgs.domain.chat.repository;

import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.UserType;

import java.util.List;

/**
 * Created by zhufre on 6/28/2016.
 */
public interface IContactRepository {
    List<ChatContact> getListContact();
    ChatContact getContactById(int id);
    ChatContact getContactByEmail(String email, UserType userType);
    void createOrUpdate(ChatContact contact);
}

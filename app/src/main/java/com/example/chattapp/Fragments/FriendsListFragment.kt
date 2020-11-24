package com.example.chattapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattapp.MainActivity
import com.example.chattapp.R
import com.example.chattapp.SendMessageActivity
import com.example.chattapp.adapter.FriendAdapter
import com.example.chattapp.model.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_friendslist.*

class FriendsListFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var friendRef: CollectionReference
    var friendsList = mutableListOf<ChatUser>()
    private lateinit var firebaseUserID: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_friendslist, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFriendDataBase()
        initFriendRecyclerView()
        getFriendListData()
    }

    private fun initFriendDataBase() {
        db = Firebase.firestore
        firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        friendRef = db.collection("users").document(firebaseUserID).collection("friendsCollection")
    }

    private fun initFriendRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        friends_recyclerView.layoutManager = layoutManager

        val listener: (ChatUser) -> Unit = {
            val intent:Intent = Intent(requireContext(), SendMessageActivity::class.java)
            intent.putExtra("FRIENDUID",it.uid)
            intent.putExtra("FRIENDUSERNAME",it.username)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        val adapter = FriendAdapter(friendsList, listener)
        friends_recyclerView.adapter = adapter

        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)
            ?.let { itemDecorator.setDrawable(it) }
        friends_recyclerView.addItemDecoration(itemDecorator)

        friends_recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun getFriendListData() {
        friendRef.get()
            .addOnSuccessListener { result ->
                friendsList.clear()
                for (document in result) {
                    val id = document.data["uid"] as String
                    val username = document.data["username"] as String
                    friendsList.add(ChatUser(id, username))
                }
                //sort by name
                friendsList.sortBy { it.username }
                refreshRecyclerView()
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    private fun refreshRecyclerView() {
        if (friends_recyclerView.adapter != null) {
            val temp = friends_recyclerView.adapter as FriendAdapter
            temp.updateDataList()
            //friends_recyclerView.scrollToPosition(temp.itemCount - 1)
        }
    }
}
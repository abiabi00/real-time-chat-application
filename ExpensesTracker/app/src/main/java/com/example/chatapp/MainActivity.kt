import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var messagesRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var messagesListener: ValueEventListener
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance()
        messagesRef = database.reference.child("messages")
        auth = FirebaseAuth.getInstance()

        // Set up RecyclerView
        messagesAdapter = MessagesAdapter()
        messagesRecyclerView.adapter = messagesAdapter

        // Set up send button click listener
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (!TextUtils.isEmpty(messageText)) {
                val message = Message(auth.currentUser!!.uid, messageText)
                messagesRef.push().setValue(message)
                messageEditText.setText("")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Attach ValueEventListener to messagesRef
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    messages.add(message!!)
                }
                messagesAdapter.setMessages(messages)
                messagesRecyclerView.smoothScrollToPosition(messagesAdapter.itemCount)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancelled event
            }
        }
        messagesRef.addValueEventListener(messagesListener)
    }

    override fun onStop() {
        super.onStop()

        // Detach ValueEventListener from messagesRef
        messagesRef.removeEventListener(messagesListener)
    }

    private data class Message(val senderId: String = "", val text: String = "")

    private class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {
        private val messages = mutableListOf<Message>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.bind(message)
        }

        override fun getItemCount(): Int = messages.size

        fun setMessages(newMessages: List<Message>) {
            messages.clear()
            messages.addAll(newMessages)
            notifyDataSetChanged()
        }

        class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(message: Message) {
                itemView.messageTextView.text = message.text
            }
        }
    }
}
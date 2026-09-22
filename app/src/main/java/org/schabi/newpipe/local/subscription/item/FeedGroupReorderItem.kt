package org.schabi.newpipe.local.subscription.item

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.schabi.newpipe.R
import org.schabi.newpipe.database.feed.model.FeedGroupEntity
import org.schabi.newpipe.databinding.FeedGroupReorderItemBinding
import org.schabi.newpipe.local.subscription.FeedGroupIcon

data class FeedGroupReorderItem(
    val groupId: Long = FeedGroupEntity.GROUP_ALL_ID,
    val name: String,
    val icon: FeedGroupIcon,
    val dragCallback: ItemTouchHelper,
    val moveCallback: (Int, Int) -> Boolean
) : BindableItem<FeedGroupReorderItemBinding>() {
    constructor(
        feedGroupEntity: FeedGroupEntity,
        dragCallback: ItemTouchHelper,
        moveCallback: (Int, Int) -> Boolean
    ) : this(
        feedGroupEntity.uid,
        feedGroupEntity.name,
        feedGroupEntity.icon,
        dragCallback,
        moveCallback
    )

    override fun getId(): Long {
        return when (groupId) {
            FeedGroupEntity.GROUP_ALL_ID -> super.getId()
            else -> groupId
        }
    }

    override fun getLayout(): Int = R.layout.feed_group_reorder_item

    override fun bind(viewBinding: FeedGroupReorderItemBinding, position: Int) {
        viewBinding.groupName.text = name
        viewBinding.groupIcon.setImageResource(icon.getDrawableRes())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bind(viewHolder: GroupieViewHolder<FeedGroupReorderItemBinding>, position: Int, payloads: MutableList<Any>) {
        super.bind(viewHolder, position, payloads)
        viewHolder.binding.handle.contentDescription = viewHolder.binding.handle.context.getString(
            R.string.reorder_item,
            name
        )
        viewHolder.binding.handle.isFocusable = true
        viewHolder.binding.handle.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        viewHolder.binding.handle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                dragCallback.startDrag(viewHolder)
                return@setOnTouchListener true
            }

            false
        }
        ViewCompat.replaceAccessibilityAction(
            viewHolder.binding.handle,
            AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD,
            viewHolder.binding.handle.context.getString(R.string.move_up)
        ) { _, _ -> move(viewHolder, -1) }
        ViewCompat.replaceAccessibilityAction(
            viewHolder.binding.handle,
            AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD,
            viewHolder.binding.handle.context.getString(R.string.move_down)
        ) { _, _ -> move(viewHolder, 1) }

        if (position <= 0) {
            ViewCompat.removeAccessibilityAction(
                viewHolder.binding.handle,
                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
            )
        }
        val itemCount = viewHolder.bindingAdapter?.itemCount ?: 0
        if (position < 0 || position >= itemCount - 1) {
            ViewCompat.removeAccessibilityAction(
                viewHolder.binding.handle,
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
            )
        }
    }

    private fun move(
        viewHolder: GroupieViewHolder<FeedGroupReorderItemBinding>,
        offset: Int
    ): Boolean {
        val source = viewHolder.bindingAdapterPosition
        return source != -1 && moveCallback(source, source + offset)
    }

    override fun getDragDirs(): Int {
        return UP or DOWN
    }

    override fun initializeViewBinding(view: View) = FeedGroupReorderItemBinding.bind(view)
}

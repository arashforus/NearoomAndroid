package com.arashforus.nearroom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import com.bumptech.glide.Glide

class MyAnimations(val context: Context) {

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 400

    fun zoomImageFromThumb(imagePath: String, startImageView : ImageView, finalImageView : ImageView) {
        // If there's an animation in progress, cancel it
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        //val expandedImageView: ImageView = finalImage
        //expandedImageView.setImageResource(imageResId)
        Glide.with(context)
            .load(imagePath)
            .into(finalImageView)

        // Calculate the starting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        startImageView.getGlobalVisibleRect(startBoundsInt)
        finalImageView.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        startImageView.alpha = 0f
        finalImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        finalImageView.pivotX = 0f
        finalImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play( ObjectAnimator.ofFloat( finalImageView, View.X, startBounds.left, finalBounds.left)).apply {
                with(ObjectAnimator.ofFloat(finalImageView, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }
                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.

        finalImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(finalImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(finalImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        startImageView.alpha = 1f
                        finalImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        startImageView.alpha = 1f
                        finalImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }

        }
    }


    fun zoomImageFromThumb(imagePath: Int, startImageView : ImageView, finalImageView : ImageView) {
        // If there's an animation in progress, cancel it
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        //val expandedImageView: ImageView = finalImage
        //expandedImageView.setImageResource(imageResId)
        finalImageView.setImageResource(imagePath)

        // Calculate the starting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        startImageView.getGlobalVisibleRect(startBoundsInt)
        finalImageView.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        startImageView.alpha = 0f
        finalImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        finalImageView.pivotX = 0f
        finalImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play( ObjectAnimator.ofFloat( finalImageView, View.X, startBounds.left, finalBounds.left)).apply {
                with(ObjectAnimator.ofFloat(finalImageView, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }
                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.

        finalImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(finalImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(finalImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(finalImageView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        startImageView.alpha = 1f
                        finalImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        startImageView.alpha = 1f
                        finalImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }

        }
    }

}
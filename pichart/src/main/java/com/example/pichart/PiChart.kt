package com.example.pichart

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.*

class PiChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Data
    private var data: PiData? = null

    // State
    private var pieState = PieState.MINIMIZED
    private var initialHeight: Int = 0
    private var selectedPiePiece = ""
    private var selectedPiePieceSweepAngle = 0f
    private var selectedPiePieceStartAngle = 0f

    // Graphics
    private val borderPaint = Paint()
    private val linePaint = Paint()
    private val indicatorCirclePaint = Paint()
    private val titleTextPaint = Paint()
    private val mainTextPaint = Paint()
    private val detailsTextPaint = Paint()
    private val oval = RectF()
    private val titleBackground = RectF()

    // Animations
    private val expandAnimator = ValueAnimator.ofInt()
    private val collapseAnimator = ValueAnimator.ofInt()
    private val textAlpha = ValueAnimator.ofInt()
    private val angleUp = ValueAnimator.ofFloat()
    private val angleDown = ValueAnimator.ofFloat()
    private val growChart = ValueAnimator.ofFloat()
    private val borderAlpha = ValueAnimator.ofInt()
    private val expandTitleLeft = ValueAnimator.ofFloat()
    private val expandTitleRight = ValueAnimator.ofFloat()
    private val shrinkChart = ValueAnimator.ofFloat()
    private val titleAlpha = ValueAnimator.ofFloat()
    private val detailsTextAlpha = ValueAnimator.ofInt()
    private val animateProjectSelection = AnimatorSet()
    private val titleCollapser = AnimatorSet()
    private val titleExpander = AnimatorSet()
    private val animateProjectDeselection = AnimatorSet()
    private val animateExpansion = AnimatorSet()
    private val animateCollapse = AnimatorSet()

    init {
        borderPaint.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.WHITE
        }
        indicatorCirclePaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.LTGRAY
            alpha = 0
        }
        linePaint.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.LTGRAY
            alpha = 0
        }
        mainTextPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            alpha = 0
        }
        titleTextPaint.apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 0
            textAlign = Paint.Align.CENTER
        }
        detailsTextPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            alpha = 0
            textAlign = Paint.Align.CENTER

        }
        setupAnimations()
    }

    /**
     * Populates the timesheetDays and sets up the view based off the new data
     *
     * @param timesheetDays the new set of timesheet data to be represented by the pie chart
     */
    fun setData(data: PiData) {
        this.data = data
        if (this.initialHeight == 0) this.initialHeight = layoutParams.height
        setPieSliceDimensions()
        invalidate()
    }

    /**
     * Calculates and sets the dimensions of the pie slices in the pie chart
     */
    private fun setPieSliceDimensions() {
        var lastAngle = 0f
        data?.pieSlices?.forEach {
            // starting angle is the location of the last angle drawn
            it.value.startAngle = lastAngle
            // sweep angle is determined by multiplying the percentage of the project time with respect
            // to the total time recorded and scaling it to unit circle degrees by multiplying by 360
            it.value.sweepAngle = (((it.value.value / data?.totalValue!!)) * 360f).toFloat()
            lastAngle += it.value.sweepAngle
            // use the angle between the start and sweep angles to help get position of the indicator circle
            // formula for x pos: (length of line) * cos(middleAngle) + (distance from left edge of screen)
            // formula for y pos: (length of line) * sin(middleAngle) + (distance from top edge of screen)
            val middleAngle = it.value.sweepAngle / 2 + it.value.startAngle

            it.value.indicatorCircleLocation.x = (layoutParams.height.toFloat() / 2 - layoutParams.height / 8) * Math.cos(Math.toRadians(middleAngle.toDouble())).toFloat() + width / 2
            it.value.indicatorCircleLocation.y = (layoutParams.height.toFloat() / 2 - layoutParams.height / 8) * Math.sin(Math.toRadians(middleAngle.toDouble())).toFloat() + layoutParams.height / 2
        }
    }

    /**
     * Set bounds for pie circle graphic and sizes for canvas graphics
     *
     * @param widthMeasureSpec width of view
     * @param heightMeasureSpec height of view
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setCircleBounds()
        setGraphicSizes()
    }

    /**
     * Sets the text sizes and thickness of graphics used in the view
     */
    private fun setGraphicSizes() {
        mainTextPaint.textSize = initialHeight / 4.5.toFloat()
        titleTextPaint.textSize = initialHeight / 3.5.toFloat()
        detailsTextPaint.textSize = initialHeight / 3.5.toFloat()
        borderPaint.strokeWidth = initialHeight / 40.toFloat()
        linePaint.strokeWidth = initialHeight / 40.toFloat()
    }

    /**
     * Sets the bounds of the pie chart
     *
     * @param top the top bound of the circle. top of view by default
     * @param bottom the bottom bound of the circle. bottom of view by default
     * @param left the left bound of the circle. half of height by default
     * @param right the right bound of the circle. hald of height by default
     */
    private fun setCircleBounds(top: Float = 0f, bottom: Float = layoutParams.height.toFloat(),
                                left: Float = (width / 2) - (layoutParams.height / 2).toFloat(),
                                right: Float = (width / 2) + (layoutParams.height / 2).toFloat()) {
        oval.top = top
        oval.bottom = bottom
        oval.left = left
        oval.right = right
    }

    /**
     * Sets the bounds for the title background animation
     */
    private fun setTitleBackgroundBounds(top: Float = 0f, bottom: Float = layoutParams.height.toFloat(), left: Float = width / 2 - (layoutParams.height / 2).toFloat(),
                                         right: Float = width / 2 + (layoutParams.height / 2).toFloat()) {
        titleBackground.top = top
        titleBackground.bottom = bottom
        titleBackground.left = left
        titleBackground.right = right
    }

    /**
     * Handles touch interaction for the view
     *
     * @param event the recorded motion event
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) return true
        if (event?.action == MotionEvent.ACTION_UP) {
            when (pieState) {
                // if pie chart is minimized, expand the chart
                PieState.MINIMIZED -> {
                    pieState = PieState.ANIMATING
                    expandDefaultPieChart()
                }
                // if pie chart is expanded, check if project was clicked
                // if project clicked, show project details
                // if project was not clicked, collapse pie chart
                PieState.EXPANDED -> {
                    pieState = PieState.ANIMATING
                    var foundEntry = false
                    data?.pieSlices?.forEach {
                        if (projectClicked(event, it.value)) {
                            foundEntry = true
                            selectedPiePiece = it.key
                            selectedPiePieceSweepAngle = it.value.sweepAngle
                            selectedPiePieceStartAngle = it.value.startAngle
                            selectProject()
                            return@forEach
                        }
                    }
                    if (!foundEntry) collapseDefaultPieChart()
                }
                // touch anywhere on view to deselect a project
                PieState.PROJECT_SELECTED -> {
                    pieState = PieState.ANIMATING
                    deselectProject()
                }
                else -> {
                } //do nothing
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Check if project was clicked
     *
     * @param event the recorded motion event
     * @param pieItem the pieItem being evaluated for a click
     */
    private fun projectClicked(event: MotionEvent, pieItem: PieSlice): Boolean {
        // if project is to the right of the pie chart
        if (pieItem.indicatorCircleLocation.x > width / 2) {
            if (event.x > pieItem.indicatorCircleLocation.x &&
                event.x < pieItem.indicatorCircleLocation.x + width / 4 &&
                event.y > pieItem.indicatorCircleLocation.y - mainTextPaint.textSize - 10 &&
                event.y < pieItem.indicatorCircleLocation.y + 20) {
                return true
            }
            // if project is to the left of the pie chart
        } else if (event.x < pieItem.indicatorCircleLocation.x &&
            event.x > pieItem.indicatorCircleLocation.x - width / 4 &&
            event.y > pieItem.indicatorCircleLocation.y - mainTextPaint.textSize - 10 &&
            event.y < pieItem.indicatorCircleLocation.y + 20) {
            return true
        }
        return false
    }

    /**
     * Draws the view onto the screen
     *
     * @param canvas canvas object to be used to draw
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        data?.pieSlices?.let { slices ->
            slices.forEach {
                // draw all pie slices except the selected one. we draw the selected one after so that
                // it can overlap these during it's selected animation
                if (it.value != data?.pieSlices!![selectedPiePiece]) {
                    canvas?.drawArc(oval, it.value.startAngle, it.value.sweepAngle, true, it.value.paint)
                    canvas?.drawArc(oval, it.value.startAngle, it.value.sweepAngle, true, borderPaint)
                    drawIndicators(canvas, it.value)
                }
            }
            // draw the selected pie piece last so that it can overlap the others during the select animation
            slices[selectedPiePiece]?.let {
                canvas?.drawArc(oval, it.startAngle, it.sweepAngle, true, it.paint)
                drawIndicators(canvas, it)
                if (pieState != PieState.PROJECT_SELECTED && pieState != PieState.ANIMATING) {
                    canvas?.drawArc(oval, it.startAngle, it.sweepAngle, true, borderPaint)
                } else {
                    canvas?.drawRoundRect(titleBackground, oval.bottom / 2, oval.bottom / 2, it.paint)
                    canvas?.drawText(it.name, width / 2.toFloat(), (oval.bottom - titleTextPaint.textSize) + 5, titleTextPaint)
                    drawDetails(canvas, it)
                }
            }
        }
    }

    /**
     * Draws the deliverable name, project name, role name, and total number of time allotted
     *
     * @param canvas the canvas used to draw onto the screen
     * @param pieItem the project information to display
     */
    private fun drawDetails(canvas: Canvas?, pieItem: PieSlice) {
        canvas?.drawText("Role", width / 2.toFloat() + width / 4.toFloat(),
            layoutParams.height / 2.5.toFloat(), detailsTextPaint)
        canvas?.drawText("Deliverable", width / 4.toFloat(),
            layoutParams.height / 2.5.toFloat(), detailsTextPaint)
        canvas?.drawText("Project", width / 4.toFloat(),
            layoutParams.height / 2.toFloat() + layoutParams.height / 4.toFloat(), detailsTextPaint)
        canvas?.drawText("Time Allotted", width / 2.toFloat() + width / 4.toFloat(),
            layoutParams.height / 2.toFloat() + layoutParams.height / 4.toFloat(), detailsTextPaint)

        canvas?.drawText("ROLE", width / 2.toFloat() + width / 4.toFloat(),
            layoutParams.height / 2.5.toFloat() + detailsTextPaint.textSize, detailsTextPaint)
        canvas?.drawText("DELIVERABLE", width / 4.toFloat(),
            layoutParams.height / 2.5.toFloat() + detailsTextPaint.textSize, detailsTextPaint)
        canvas?.drawText("PROJECT NAME", width / 4.toFloat(),
            layoutParams.height / 2.toFloat() + layoutParams.height / 4.toFloat() + detailsTextPaint.textSize, detailsTextPaint)
        canvas?.drawText("HOURS", width / 2.toFloat() + width / 4.toFloat(),
            layoutParams.height / 2.toFloat() + layoutParams.height / 4.toFloat() + detailsTextPaint.textSize, detailsTextPaint)
    }

    /**
     * Draws the indicators for projects displayed on the pie chart
     *
     * @param canvas the canvas used to draw onto the screen
     * @param pieItem the project information to display
     */
    private fun drawIndicators(canvas: Canvas?, pieItem: PieSlice) {
        // draw line & text for indicator circle if on left side of the pie chart
        if (pieItem.indicatorCircleLocation.x < width / 2) {
            canvas?.drawLine(pieItem.indicatorCircleLocation.x, pieItem.indicatorCircleLocation.y,
                pieItem.indicatorCircleLocation.x - width / 4, pieItem.indicatorCircleLocation.y, linePaint)
            mainTextPaint.textAlign = Paint.Align.LEFT
            canvas?.drawText(pieItem.name, pieItem.indicatorCircleLocation.x - width / 4, pieItem.indicatorCircleLocation.y - 10, mainTextPaint)
            // draw line & text for indicator circle if on right side of the pie chart
        } else {
            canvas?.drawLine(pieItem.indicatorCircleLocation.x, pieItem.indicatorCircleLocation.y,
                pieItem.indicatorCircleLocation.x + width / 4, pieItem.indicatorCircleLocation.y, linePaint)
            mainTextPaint.textAlign = Paint.Align.RIGHT
            canvas?.drawText(pieItem.name, pieItem.indicatorCircleLocation.x + width / 4, pieItem.indicatorCircleLocation.y - 10, mainTextPaint)
        }
        // draw indicator circles for pie slice
        canvas?.drawCircle(pieItem.indicatorCircleLocation.x, pieItem.indicatorCircleLocation.y, initialHeight / 30f, indicatorCirclePaint)
    }

    /**
     * Expands the pie chart from it's default minimized state
     */
    private fun expandDefaultPieChart() {
        expandAnimator.setIntValues(layoutParams.height, (width / 2.5).toInt())
        textAlpha.setIntValues(0, 255)
        animateExpansion.play(expandAnimator).with(textAlpha)
        animateExpansion.start()
    }

    /**
     * Collapses the pie chart down to it's default minimized state
     */
    private fun collapseDefaultPieChart() {
        collapseAnimator.setIntValues(layoutParams.height, initialHeight)
        textAlpha.setIntValues(255, 0)
        animateCollapse.play(collapseAnimator).with(textAlpha)
        animateCollapse.start()
    }

    /**
     * Animates the selection of a project
     */
    private fun selectProject() {
        expandTitleRight.setFloatValues(width / 2.toFloat(), width / 2.toFloat() + width / 4.toFloat())
        expandTitleRight.interpolator = OvershootInterpolator()
        expandTitleLeft.setFloatValues(width / 2.toFloat(), width / 4.toFloat())
        expandTitleLeft.interpolator = OvershootInterpolator()

        shrinkChart.setFloatValues(layoutParams.height.toFloat(), layoutParams.height.toFloat() / 4)
        borderAlpha.setIntValues(255, 0)
        angleUp.setFloatValues(selectedPiePieceSweepAngle, 360f)
        textAlpha.setIntValues(255, 0)
        titleAlpha.setIntValues(0, 255)
        detailsTextAlpha.setIntValues(0, 255)

        animateProjectSelection.play(angleUp).with(borderAlpha).with(textAlpha).before(shrinkChart)
        titleExpander.play(expandTitleRight).with(expandTitleLeft).with(titleAlpha).after(animateProjectSelection).before(detailsTextAlpha)
        titleExpander.start()
    }

    /**
     * Animates the deselection of a project
     */
    private fun deselectProject() {
        expandTitleRight.setFloatValues(width / 2.toFloat() + width / 4.toFloat(), width / 2.toFloat())
        expandTitleRight.interpolator = AnticipateInterpolator()
        expandTitleLeft.setFloatValues(width / 4.toFloat(), width / 2.toFloat())
        expandTitleLeft.interpolator = AnticipateInterpolator()

        growChart.setFloatValues(oval.bottom, width / 2.5.toFloat())
        angleDown.setFloatValues(360f, selectedPiePieceSweepAngle)
        borderAlpha.setIntValues(0, 255)
        textAlpha.setIntValues(0, 255)
        titleAlpha.setIntValues(255, 0)
        detailsTextAlpha.setIntValues(255, 0)

        titleCollapser.play(expandTitleLeft).with(expandTitleRight).with(titleAlpha).after(detailsTextAlpha)
        animateProjectDeselection.play(titleCollapser).before(growChart).with(borderAlpha).before(angleDown).before(textAlpha)
        animateProjectDeselection.start()
    }

    /**
     * Initialize animations properties for all used animations
     */
    private fun setupAnimations() {
        // Expands pie chart from minimized state
        expandAnimator.duration = 200
        expandAnimator.interpolator = OvershootInterpolator()
        expandAnimator.addUpdateListener {
            layoutParams.height = it.animatedValue as Int
            requestLayout()
            setCircleBounds()
            setPieSliceDimensions()
            invalidate()
        }
        expandAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                pieState = PieState.EXPANDED
            }
        })

        // Collapses pie chart from expanded state
        collapseAnimator.duration = 200
        collapseAnimator.interpolator = DecelerateInterpolator()
        collapseAnimator.addUpdateListener {
            layoutParams.height = it.animatedValue as Int
            requestLayout()
            setCircleBounds()
            setPieSliceDimensions()
            invalidate()
        }
        collapseAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                pieState = PieState.MINIMIZED
            }
        })

        // Animates showing and hiding project titles
        textAlpha.duration = 300
        textAlpha.interpolator = DecelerateInterpolator()
        textAlpha.addUpdateListener {
            mainTextPaint.alpha = it.animatedValue as Int
            linePaint.alpha = it.animatedValue as Int
            indicatorCirclePaint.alpha = it.animatedValue as Int
            invalidate()
        }

        // Animates showing and hiding selected project title
        titleAlpha.duration = 200
        titleAlpha.interpolator = DecelerateInterpolator()
        titleAlpha.addUpdateListener {
            titleTextPaint.alpha = it.animatedValue as Int
            invalidate()
        }

        // Animates showing and hiding details of selected project
        detailsTextAlpha.duration = 200
        detailsTextAlpha.interpolator = DecelerateInterpolator()
        detailsTextAlpha.addUpdateListener {
            detailsTextPaint.alpha = it.animatedValue as Int
            invalidate()
        }

        // Animates showing and hiding borders on pie slices
        borderAlpha.duration = 300
        borderAlpha.interpolator = DecelerateInterpolator()
        borderAlpha.addUpdateListener {
            borderPaint.alpha = it.animatedValue as Int
            invalidate()
        }

        // Animates pie slice filling rest of pie
        angleUp.duration = 350
        angleUp.interpolator = DecelerateInterpolator()
        angleUp.addUpdateListener { valueAnimator ->
            data?.pieSlices!![selectedPiePiece]?.sweepAngle = valueAnimator.animatedValue as Float
            invalidate()
        }
        angleUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                pieState = PieState.PROJECT_SELECTED
            }
        })

        // Animates pie slice un-filling back to default location
        angleDown.duration = 300
        angleDown.interpolator = AccelerateDecelerateInterpolator()
        angleDown.addUpdateListener { valueAnimator ->
            data?.pieSlices!![selectedPiePiece]?.sweepAngle = valueAnimator.animatedValue as Float
            invalidate()
        }
        angleDown.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                pieState = PieState.EXPANDED
            }
        })

        // Animates shrinking the chart to show selected project
        shrinkChart.duration = 200
        shrinkChart.interpolator = DecelerateInterpolator()
        shrinkChart.addUpdateListener {
            setCircleBounds(0f, it.animatedValue as Float,
                width / 2 - (it.animatedValue as Float) / 2,
                width / 2 + (it.animatedValue as Float) / 2)
            invalidate()
        }
        shrinkChart.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                pieState = PieState.PROJECT_SELECTED
                setTitleBackgroundBounds(0f, oval.bottom, oval.left, oval.right)
            }
        })

        // Animates growing the chart after deselecting project
        growChart.duration = 350
        growChart.startDelay = 100
        growChart.interpolator = DecelerateInterpolator()
        growChart.addUpdateListener {
            setCircleBounds(0f, it.animatedValue as Float,
                width / 2 - (it.animatedValue as Float) / 2,
                width / 2 + (it.animatedValue as Float) / 2)
            invalidate()
        }

        // Animates expanding the title background to the left
        expandTitleLeft.duration = 200
        expandTitleLeft.interpolator = OvershootInterpolator()
        expandTitleLeft.addUpdateListener {
            titleBackground.left = it.animatedValue as Float
            invalidate()
        }

        // Animates expanding the title background to the right
        expandTitleRight.duration = 200
        expandTitleRight.interpolator = OvershootInterpolator()
        expandTitleRight.addUpdateListener {
            titleBackground.right = it.animatedValue as Float
            invalidate()
        }
    }
}
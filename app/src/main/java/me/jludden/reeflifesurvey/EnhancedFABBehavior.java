package me.jludden.reeflifesurvey;

import android.animation.Animator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Created by Jason on 8/20/2017.
 *
 * Thanks to https://github.com/ianhanniballake/cheesesquare/blob/92bcf7c8b57459051424cd512a032c12d24a41b3/app/src/main/java/com/support/android/designlibdemo/ScrollAwareFABBehavior.java
 */

public class EnhancedFABBehavior extends FloatingActionButton.Behavior {

    private boolean mFabHidden = false;
    private MainActivity mMain;
    private final int FAB_ONSCROLL_ANIMATION_DURATION = 200;

    public EnhancedFABBehavior(Context context, AttributeSet attributeSet){
        super();
        mMain = (MainActivity) context;
    }

        //region SCROLL BEHAVIOR
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,dyUnconsumed);

        if (dyConsumed > 0 && !mFabHidden) { // User scrolled down and the FAB is currently visible -> hide the FAB
            mFabHidden = true;
            hideFabAnimation(child);
            mMain.hideFABmenu();

        } else if (dyConsumed < 0) { // User scrolled up and the FAB is currently not visible -> show the FAB
            child.setVisibility(View.VISIBLE);
            mFabHidden = false;
            YoYo.with(Techniques.SlideInUp)
                    .duration(FAB_ONSCROLL_ANIMATION_DURATION)
                    .interpolate(new OvershootInterpolator())
                    .playOn(child);
        }
    }

    //hide the button after the animation plays
    private void hideFabAnimation(final View child){
        YoYo.with(Techniques.SlideOutDown)
                .duration(FAB_ONSCROLL_ANIMATION_DURATION)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        child.setVisibility(View.INVISIBLE);
                    }
                })
                .playOn(child);
    }
    //endregion

//    //region additional behavior to play nice with snackbars and bottom sheets
//    //TODO - could just always hide the menu when a button is clicked, so we don't need this to handle snackbars
//    @Override
//    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
//        Log.d("jludden.reeflifesurvey"  ,"FAB layoutDependsOn "+(dependency instanceof Snackbar.SnackbarLayout)
//                +" "+ (dependency instanceof android.widget.LinearLayout) + " " + dependency.isShown() + "/n" + dependency.toString());
//        //return (dependency instanceof Snackbar.SnackbarLayout );
//        return isDependent(dependency); //dependency instanceof Snackbar.SnackbarLayout||dependency.getId()==R.id.bottom_sheet);
//        //return (dependency instanceof Snackbar.SnackbarLayout || dependency instanceof android.widget.LinisearLayout);
//    }
//
//    @Override
//    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
//        Log.d("jludden.reeflifesurvey"  ,"onDependentViewChanged FAB TRANSLATION FOR SNACKBAR");
////        if (dependency instanceof Snackbar.SnackbarLayout || dependency instanceof android.widget.LinearLayout) {
//
//        if (isDependent(dependency)){
//            updateFabTranslationForSnackbar(child, dependency);
//            return true;
//        }
//        return false;
//    }
//
//    private void updateFabTranslationForSnackbar(FloatingActionButton child, View dependency) {
//        Log.d("jludden.reeflifesurvey"  ,"updateFabTranslationForSnackbar UPDATE FAB TRANSLATION FOR SNACKBAR");
////        float translationY = Math.min(0, ViewCompat.getTranslationY(dependency) - dependency.getHeight());
//        float translationY =  ViewCompat.getTranslationY(dependency) - dependency.getHeight();
//
//        ViewCompat.setTranslationY(child, translationY); //TODO use this to move to the left of the button menu
//
//        //ViewCompat.setTranslationX(child, translationY); //TODO use this to move to the left of the button menu
//    }
//
//    @Override
//    public void onDependentViewRemoved(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
//        Log.d("jludden.reeflifesurvey"  ,"onDependentViewRemoved FAB TRANSLATION FOR SNACKBAR"+isDependent(dependency)+(ViewCompat.getTranslationY(child) != 0.0F));
//        float translationY =  ViewCompat.getTranslationY(child);
//        if (isDependent(dependency) && ViewCompat.getTranslationY(child) != 0.0F){
//        //if (dependency instanceof Snackbar.SnackbarLayout && ViewCompat.getTranslationY(child) != 0.0F) {
//            ViewCompat.animate(child).translationY(translationY).scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setInterpolator(new FastOutSlowInInterpolator()).start();
//        }
//    }
//
//    private boolean isDependent(View dependency){
//        if(!dependency.isShown()) return false;
//        return (dependency instanceof Snackbar.SnackbarLayout||dependency.getId()==R.id.bottom_sheet);
//    }

    //Todo methods below copied from FloatingActionButton.Behavior
    //onlayout child stuff
//    private boolean updateFabVisibilityForBottomSheet(View bottomSheet,
//                                                      FloatingActionButton child) {
//        if (!shouldUpdateVisibility(bottomSheet, child)) {
//            return false;
//        }
//        CoordinatorLayout.LayoutParams lp =
//                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
////        if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
////            child.hide(mInternalAutoHideListener, false);
////        } else {
////            child.show(mInternalAutoHideListener, false);
////        }
//        return true;
//    }
//
//    private boolean shouldUpdateVisibility(View dependency, FloatingActionButton child) {
//        final CoordinatorLayout.LayoutParams lp =
//                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
////        if (!mAutoHideEnabled) {
////            return false;
////        }
//
//        if (lp.getAnchorId() != dependency.getId()) {
//            // The anchor ID doesn't match the dependency, so we won't automatically
//            // show/hide the FAB
//            return false;
//        }
//
//        //noinspection RedundantIfStatement
////        if (child.getUserSetVisibility() != VISIBLE) {
////            // The view isn't set to be visible so skip changing its visibility
////            return false;
////        }
//
//        return true;
//    }
//
//    @Override
//    public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child,
//                                 int layoutDirection) {
//        Log.d("jludden.reeflifesurvey"  ,"onLayoutChild FAB ");
//
//        // First, let's make sure that the visibility of the FAB is consistent
//        final List<View> dependencies = parent.getDependencies(child);
//        for (int i = 0, count = dependencies.size(); i < count; i++) {
//            final View dependency = dependencies.get(i);
//            Log.d("jludden.reeflifesurvey"  ,"onLayoutChild FAB stuff"+isDependent(dependency)+" ? "+(dependency instanceof Snackbar.SnackbarLayout||dependency.getId()==R.id.bottom_sheet));
//            if(dependency instanceof Snackbar.SnackbarLayout||dependency.getId()==R.id.bottom_sheet){
//            //if (isDependent(dependency)) {
//                if (updateFabVisibilityForBottomSheet(dependency, child)) {
//                    break;
//                }
//
////            if (dependency instanceof AppBarLayout) {
////                if (updateFabVisibilityForAppBarLayout(
////                        parent, (AppBarLayout) dependency, child)) {
////                    break;
////                }
////            } else if (isBottomSheet(dependency)) {
////                if (updateFabVisibilityForBottomSheet(dependency, child)) {
////                    break;
////                }
////            }
//            }
//        }
//        // Now let the CoordinatorLayout lay out the FAB
//        parent.onLayoutChild(child, layoutDirection);
//        // Now offset it if needed
//        //offsetIfNeeded(parent, child); todo handle pre lollipop
//        return true;
//    }
    //endregion


}

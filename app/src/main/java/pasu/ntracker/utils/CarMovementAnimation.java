package pasu.ntracker.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

/**
 * Created by VImal on 9/1/18.
 */

public class CarMovementAnimation {
    private static CarMovementAnimation carAnimation;

    public static CarMovementAnimation getInstance() {
        if (carAnimation == null) carAnimation = new CarMovementAnimation();
        return carAnimation;
    }

    /**
     * new animation
     *
     * @param marker marker instance
     */
    public void addMarkerAnimate(final Marker marker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && marker != null) {
            //for memory issue to avoid in lower drivers
            ValueAnimator ani = ValueAnimator.ofFloat(0, 1); //change for (0,1) if you want a fade in
            ani.setDuration(2000);
            ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    marker.setAlpha((float) animation.getAnimatedValue());
                    //marker.setAnchor(1f, (Float) animation.getAnimatedValue());
                }
            });
            ani.start();
            // dropPinEffect(marker);
        }
    }

    /**
     * Remove marker with  value animation
     *
     * @param removeMarker
     */
    public void removeMarkerWithAnimation(final Marker removeMarker) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            //for memory issue to avoid in lower drivers
            removeMarker.remove();
            return;
        }
        final ValueAnimator ani = ValueAnimator.ofFloat(1, 0); //change for (0,1) if you want a fade in
        ani.setDuration(3000);
        ani.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeMarker.remove();
            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                removeMarker.setAlpha((float) animation.getAnimatedValue());
            }

        });
        ani.start();
    }

    /**
     * @param marker
     * @param newLatLng
     * @param bearing
     */
    public synchronized void animateMarker(final Marker marker, final LatLng newLatLng, float bearing) {
        if (bearing < 0) {
            bearing = 0;
        }
        if (marker != null) {
            ValueAnimator valueAnimator = new ValueAnimator();
            final LatLng startPosition = marker.getPosition();
            final float startRotation = marker.getRotation();
            final float angle = 180 - Math.abs(Math.abs(startRotation - bearing) - 180);
            final float right = WhichWayToTurn(startRotation, bearing);
            final float finalBearing = bearing;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = SphericalUtil.interpolate(startPosition, newLatLng, v);
                        float rotation = startRotation + right * v * angle;
//                        marker.setRotation((float) rotation);
                        marker.setRotation(computeRotation(v, startRotation, finalBearing));
                        marker.setPosition(newPosition);


                    } catch (Exception ex) {
                        // I don't care atm..
                        ex.printStackTrace();
                    }
                }
            });
            valueAnimator.setFloatValues(0, 1);
            valueAnimator.setDuration(2500);
            valueAnimator.start();
        }
    }

    private float WhichWayToTurn(float currentDirection, float targetDirection) {
        float diff = targetDirection - currentDirection;
        if (Math.abs(diff) == 0) {
            return 0;
        }
        if (diff > 180) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Method to compute rotation (or marker's bearing) for specified fraction of animation.
     * Marker is rotated in the direction which is closer from start to end.
     *
     * @param fraction  Fraction of animation completed b/w start and end location
     * @param start 	Rotation (or Bearing) for animation's start location
     * @param end 		Rotation (or Bearing) for animation's end location
     **/
    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    public void StopMovement(){
        carAnimation = null;
    }

}

package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.simulation2D.Derivative;
import se.oru.coordination.coordination_oru.simulation2D.State;

public class RK4 {
    public static void integrate(State state, double time, double deltaTime, boolean slowDown, double MAX_VELOCITY, double MAX_VELOCITY_DAMPENING_FACTOR, double MAX_ACCELERATION) {
        synchronized (state) {
            Derivative a = Derivative.evaluate(state, time, 0.0, new Derivative(), slowDown, MAX_VELOCITY, MAX_VELOCITY_DAMPENING_FACTOR, MAX_ACCELERATION);
            Derivative b = Derivative.evaluate(state, time, deltaTime / 2.0, a, slowDown, MAX_VELOCITY, MAX_VELOCITY_DAMPENING_FACTOR, MAX_ACCELERATION);
            Derivative c = Derivative.evaluate(state, time, deltaTime / 2.0, b, slowDown, MAX_VELOCITY, MAX_VELOCITY_DAMPENING_FACTOR, MAX_ACCELERATION);
            Derivative d = Derivative.evaluate(state, time, deltaTime, c, slowDown, MAX_VELOCITY, MAX_VELOCITY_DAMPENING_FACTOR, MAX_ACCELERATION);

            double dxdt = (1.0f / 6.0f) * (a.getVelocity() + 2.0f * (b.getVelocity() + c.getVelocity()) + d.getVelocity());
            double dvdt = (1.0f / 6.0f) * (a.getAcceleration() + 2.0f * (b.getAcceleration() + c.getAcceleration()) + d.getAcceleration());

            state.setPosition(state.getPosition() + dxdt * deltaTime);
            state.setVelocity(state.getVelocity() + dvdt * deltaTime);
        }
    }
}

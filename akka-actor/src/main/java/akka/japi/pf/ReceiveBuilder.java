/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.japi.pf;

/**
 * Used for building a partial function for {@link akka.actor.Actor#receive() Actor.receive()}.
 *
 * There is both a match on type only, and a match on type and predicate.
 *
 * Inside an actor you can use it like this with Java 8 to define your receive method.
 * <p/>
 * Example:
 * <pre>
 * @Override
 * public PartialFunction<Object, BoxedUnit> receive() {
 *   return ReceiveBuilder.
 *     match(Double.class, d -> {
 *       sender().tell(d.isNaN() ? 0 : d, self());
 *     }).
 *     match(Integer.class, i -> {
 *       sender().tell(i * 10, self());
 *     }).
 *     match(String.class, s -> s.startsWith("foo"), s -> {
 *       sender().tell(s.toUpperCase(), self());
 *     }).build();
 * }
 * </pre>
 *
 * This is an EXPERIMENTAL feature and is subject to change until it has received more real world testing.
 */
public class ReceiveBuilder {
  private ReceiveBuilder() {
  }

  /**
   * Return a new {@link UnitPFBuilder} with a case statement added.
   *
   * @param type   a type to match the argument against
   * @param apply  an action to apply to the argument if the type matches
   * @return       a builder with the case statement added
   */
  public static <P> UnitPFBuilder<Object> match(final Class<P> type, FI.UnitApply<P> apply) {
    return UnitMatch.match(type, apply);
  }

  /**
   * Return a new {@link UnitPFBuilder} with a case statement added.
   *
   * @param type       a type to match the argument against
   * @param predicate  a predicate that will be evaluated on the argument if the type matches
   * @param apply      an action to apply to the argument if the type matches and the predicate returns true
   * @return           a builder with the case statement added
   */
  public static <P> UnitPFBuilder<Object> match(final Class<P> type,
                                                FI.TypedPredicate<P> predicate,
                                                FI.UnitApply<P> apply) {
    return UnitMatch.match(type, predicate, apply);
  }

  /**
   * Return a new {@link UnitPFBuilder} with a case statement added.
   *
   * @param object  the object to compare equals with
   * @param apply  an action to apply to the argument if the object compares equal
   * @return a builder with the case statement added
   */
  public static <P> UnitPFBuilder<Object> matchEquals(P object, FI.UnitApply<P> apply) {
    return UnitMatch.matchEquals(object, apply);
  }

  /**
   * Return a new {@link UnitPFBuilder} with a case statement added.
   *
   * @param apply      an action to apply to the argument
   * @return           a builder with the case statement added
   */
  public static UnitPFBuilder<Object> matchAny(FI.UnitApply<Object> apply) {
    return UnitMatch.matchAny(apply);
  }

}

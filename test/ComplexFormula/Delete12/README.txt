The formula ''
describes such a property:
Whenever some user deletes some data 'v' which is NOT unknown
in `database 1`, it implies that:
Either: sometime in the future there will be some user delete 
the same data 'v' in `database 2`;
Or: (some time in the past, some user inserted the data 'v' to `database 1`,
and there never existed, and will never be a user such that he/she inserted
to `database 2` the data 'v').

There is no way to check the value after slicing, so in the event action
method, data field is checked to eliminate the events that will not 
cause violations of the property.
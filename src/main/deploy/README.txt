swerve xbox controller
left joystick = swerve translation
right joystick = winch control
triggers = swerve rotation
bumpers = rotate by 45 degrees
pov buttons = PID
start = reset gyro
a = line up to target and shoot
b = 
x = linkage
y = enable winch -- dead mans since winch when rope not up breaks it
menu/start = zero gyro
-------------------------
mech xbox controller
left joystick + y = storage control
right joystick = manual shooter control
triggers = forward / backward intake
bumpers = minor shooter adjustments
start = spin spinner
pov buttons = ?
a = hold to toggle automatic shooter
b = shooter flap
x = intake solenoids
y = hold to enable storage controll 
-------------------------
winch control - double
lining up control - boolean (true=line up, false=nothing)
shooter control - boolean & double (true=use auto control, false=use double val)
storage control - boolean & double (false=use auto control, true= use double val)
intake - boolean & double (true=out, false=in, speed)
linkage - boolean (true=up, false=down)
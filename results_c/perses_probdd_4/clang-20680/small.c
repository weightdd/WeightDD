typedef
char
int8_t
;
typedef
short
int16_t
;
typedef
int
int32_t
;
typedef
unsigned
char
uint8_t
;
typedef
short
uint16_t
;
typedef
unsigned
uint32_t
;
int8_t
safe_div_func_int8_t_s_s
(
int8_t
si1
,
int8_t
si2
)
{
return
si2
==
0
?
si1
:
si1
/
si2
;
}
int8_t
safe_rshift_func_int8_t_s_u
(
int8_t
left
,
unsigned
right
)
{
return
left
>>
right
;
}
int16_t
safe_mul_func_int16_t_s_s
(
int16_t
si1
,
int16_t
si2
)
{
return
si1
*
si2
;
}
int16_t
safe_mod_func_int16_t_s_s
(
int16_t
si1
,
int16_t
si2
)
{
return
si2
==
0
||
si1
&&
si2
==
1
?
si1
:
si1
%
si2
;
}
int16_t
safe_lshift_func_int16_t_s_s
(
int16_t
left
,
int
right
)
{
return
left
||
right
<
0
||
32767
>>
right
;
}
int32_t
safe_mod_func_int32_t_s_s
(
int32_t
si1
,
int32_t
si2
)
{
return
si1
%
si2
;
}
uint8_t
safe_add_func_uint8_t_u_u
(
uint8_t
ui1
,
uint8_t
ui2
)
{
return
ui1
+
ui2
;
}
uint8_t
safe_mod_func_uint8_t_u_u
(
uint8_t
ui1
,
uint8_t
ui2
)
{
return
ui2
==
0
?
ui1
:
ui1
%
ui2
;
}
uint16_t
safe_mod_func_uint16_t_u_u
(
uint16_t
ui1
,
uint16_t
ui2
)
{
return
ui2
==
0
;
}
uint16_t
safe_div_func_uint16_t_u_u
(
uint16_t
ui1
,
uint16_t
ui2
)
{
return
ui2
;
}
int32_t
g_4
;
uint8_t
g_27
[
]
;
int16_t
g_185
;
int32_t
g_1503
;
uint16_t
func_65
(
uint8_t
,
uint32_t
)
;
static
uint8_t
func_85
(
uint8_t
,
uint16_t
,
int8_t
)
;
int32_t
func_1
(
)
{
uint32_t
l_2982
;
int8_t
l_1959
=
0x7CL
;
if
(
g_27
[
6
]
!=
func_65
(
g_4
,
0
)
)
g_1503
^=
l_1959
;
return
l_2982
;
}
uint16_t
func_65
(
uint8_t
p_66
,
uint32_t
p_67
)
{
int32_t
l_311
=
0xE02EBC21L
;
for
(
p_66
=
-
14
;
p_66
>=
51
;
++
p_66
)
{
int8_t
l_241
;
func_85
(
p_66
,
g_27
[
6
]
,
p_66
)
;
if
(
p_67
)
;
else
{
int
i
;
func_85
(
p_66
,
g_27
[
6
]
,
p_66
)
;
for
(
;
l_241
;
++
l_241
)
{
l_311
&=
safe_div_func_int8_t_s_s
(
g_4
,
p_66
)
;
for
(
;
i
;
i
++
)
;
for
(
p_67
=
11
;
p_67
>=
12
;
)
;
}
}
}
return
p_66
;
}
uint8_t
func_85
(
uint8_t
p_86
,
uint16_t
p_87
,
int8_t
p_88
)
{
uint32_t
l_91
;
if
(
p_87
)
{
uint8_t
l_100
[
4
]
[
5
]
;
int32_t
l_120
=
0x5CE3A63AL
;
for
(
;
p_87
<
25
;
++
p_87
)
{
int16_t
l_105
[
7
]
;
for
(
;
l_91
<=
3
;
l_91
+=
1
)
l_120
=
safe_mul_func_int16_t_s_s
(
safe_rshift_func_int8_t_s_u
(
safe_mod_func_int16_t_s_s
(
safe_div_func_uint16_t_u_u
(
0xB072L
,
l_91
)
,
safe_mod_func_int32_t_s_s
(
l_105
[
l_91
]
,
0xC767A1E7L
)
)
,
6
)
,
safe_mod_func_uint16_t_u_u
(
0xD326L
,
safe_lshift_func_int16_t_s_s
(
safe_mod_func_uint8_t_u_u
(
0x4AL
,
l_100
[
l_91
]
[
l_91
]
)
,
safe_add_func_uint8_t_u_u
(
0x8538L
,
l_100
[
l_91
]
[
1
]
)
!=
p_86
^
l_120
)
)
)
;
}
}
else
g_185
=
2L
;
return
6
;
}
int
main
(
)
{
func_1
(
)
;
}

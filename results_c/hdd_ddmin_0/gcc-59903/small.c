typedef
char
int8_t
;
typedef
int
int16_t
;
typedef
int
int32_t
;
typedef
char
uint8_t
;
typedef
int
uint16_t
;
typedef
int
uint32_t
;
int16_t
(
safe_mul_func_int16_t_s_s
)
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
;
}
int32_t
(
safe_mod_func_int32_t_s_s
)
(
int32_t
si1
,
int32_t
si2
)
{
return
(
(
si2
)
)
?
(
(
si1
)
)
:
(
si1
)
;
}
uint8_t
(
safe_lshift_func_uint8_t_u_s
)
(
uint8_t
left
,
int
right
)
{
return
(
(
left
>
(
(
right
)
)
)
)
?
(
(
left
)
)
:
(
left
<<
(
(
int
)
right
)
)
;
}
struct
S0
{
int8_t
f0
;
int8_t
f1
;
uint32_t
f2
;
int32_t
f3
;
uint16_t
f4
;
}
;
uint16_t
g_32
;
int32_t
g_57
;
int32_t
g_58
;
int32_t
g_80
;
uint32_t
g_81
[
4
]
;
struct
S0
g_152
[
2
]
;
struct
S0
func_129
(
)
;
uint8_t
func_99
(
p_100
)
{
uint8_t
l_110
;
struct
S0
l_147
=
{
5L
}
;
if
(
(
(
(
(
(
l_110
)
)
,
(
(
(
(
(
(
(
(
(
(
(
(
func_129
(
(
(
(
(
(
(
(
(
(
(
(
0x2B768FFDL
)
)
)
)
)
)
)
)
)
)
)
,
l_147
)
,
p_100
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
{
}
return
p_100
;
}
struct
S0
func_129
(
int32_t
p_130
,
struct
S0
p_131
,
uint32_t
p_132
)
{
struct
S0
l_151
;
int32_t
l_195
;
for
(
;
;
)
{
if
(
g_81
[
l_195
]
)
break
;
for
(
p_132
=
0
;
(
p_132
<=
39
)
;
++
p_132
)
{
int32_t
l_164
;
for
(
p_131
.
f0
=
0
;
(
p_131
.
f0
<=
2
)
;
p_131
.
f0
+=
1
)
{
struct
S0
l_166
;
int32_t
l_200
;
g_152
[
0
]
=
l_151
;
l_151
.
f3
=
g_81
[
p_131
.
f0
]
;
{
struct
S0
l_196
;
{
uint32_t
l_177
;
g_80
=
(
(
(
(
(
(
(
(
l_164
^
(
(
g_58
|
(
(
(
(
(
(
(
(
(
(
l_177
!=
(
g_57
==
(
(
(
(
(
(
(
(
l_195
=
(
safe_mul_func_int16_t_s_s
(
(
(
(
(
(
safe_lshift_func_uint8_t_u_s
(
(
safe_mod_func_int32_t_s_s
(
(
g_152
[
0
]
.
f3
)
,
0xBCA6F5D4L
)
)
,
2
)
)
)
)
&&
p_131
.
f1
)
)
,
g_152
[
0
]
.
f4
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
)
<=
l_166
.
f2
)
==
(
10L
)
)
)
)
<
p_131
.
f1
)
)
;
{
g_152
[
0
]
=
l_196
;
}
}
for
(
;
(
l_200
)
;
l_200
+=
1
)
{
for
(
;
(
g_32
)
;
)
{
}
}
}
}
}
}
return
p_131
;
}
int
main
(
)
{
}

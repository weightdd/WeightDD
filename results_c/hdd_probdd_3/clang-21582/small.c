typedef
signed
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
uint8_t
;
typedef
unsigned
uint16_t
;
typedef
unsigned
uint32_t
;
int
printf
(
const
char
*
,
...
)
;
void
platform_main_end
(
uint32_t
crc
,
int
flag
)
{
printf
(
"checksum = %X\n"
,
crc
)
;
}
int32_t
(
safe_sub_func_int32_t_s_s
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
(
si1
)
)
)
;
}
uint16_t
(
safe_mul_func_uint16_t_u_u
)
(
uint16_t
ui1
,
uint16_t
ui2
)
{
return
(
(
unsigned
)
ui1
)
;
}
uint32_t
crc32_tab
[
256
]
;
uint32_t
crc32_context
;
void
crc32_gentab
(
)
{
uint32_t
crc
;
int
i
;
for
(
i
=
0
;
i
<
256
;
i
++
)
{
crc
=
i
;
crc32_tab
[
i
]
=
crc
;
}
}
void
crc32_byte
(
b
)
{
crc32_context
=
(
(
crc32_context
)
)
^
crc32_tab
[
(
crc32_context
^
b
)
]
;
}
void
crc32_8bytes
(
val
)
{
crc32_byte
(
(
val
)
)
;
}
void
transparent_crc
(
uint32_t
val
,
char
*
vname
,
int
flag
)
{
crc32_8bytes
(
val
)
;
}
struct
S0
{
int32_t
f0
;
uint16_t
f1
;
}
;
struct
S1
{
uint16_t
f2
;
}
;
struct
S2
{
int32_t
f0
;
uint8_t
f1
;
uint32_t
f3
;
int16_t
f4
;
int16_t
f5
;
uint16_t
f6
;
}
;
struct
S0
g_5
[
1
]
;
uint32_t
g_16
;
uint16_t
g_22
;
struct
S2
g_28
;
struct
S2
g_76
[
5
]
[
4
]
[
10
]
;
int8_t
g_159
;
int32_t
g_162
;
struct
S2
g_290
;
struct
S1
g_415
[
4
]
[
3
]
;
int32_t
g_447
;
int16_t
g_584
;
int16_t
g_766
;
int32_t
g_1160
;
int32_t
func_6
(
int32_t
,
uint32_t
,
uint32_t
)
;
int32_t
func_20
(
int8_t
)
;
uint32_t
func_30
(
)
;
int8_t
func_48
(
)
;
uint8_t
func_61
(
)
;
uint32_t
func_1
(
)
{
uint32_t
l_10
=
0x80EBBD48L
;
struct
S2
l_1174
[
]
=
{
{
0x1747EB85L
}
,
{
0L
}
,
{
0L
}
,
{
0L
}
,
{
0x1747EB85L
}
,
{
0L
}
}
;
g_1160
^=
(
g_5
,
func_6
(
l_10
,
g_5
[
0
]
.
f0
,
l_10
)
)
;
return
l_1174
[
5
]
.
f3
;
}
int32_t
func_6
(
int32_t
p_7
,
uint32_t
p_8
,
uint32_t
p_9
)
{
int16_t
l_854
;
struct
S2
l_872
=
{
0xB5C80C62L
,
0xAEL
,
1UL
,
0x456CL
,
0x0154L
}
;
for
(
;
;
)
{
uint32_t
l_13
[
]
[
6
]
[
7
]
=
{
{
{
0x881DDA8DL
}
,
{
4294967295UL
}
,
{
0x6B51F0EFL
}
,
{
0x6B51F0EFL
}
,
{
0xFC0336AEL
}
,
{
0x0335900BL
,
0x6B51F0EFL
,
0x407374FDL
,
0x6B51F0EFL
,
0x0335900BL
,
0x407374FDL
,
4294967295UL
}
}
,
{
4294967295UL
}
,
{
{
0x6B51F0EFL
}
}
,
{
0x0335900BL
}
,
{
4294967295UL
}
,
{
4294967290UL
}
}
;
int32_t
l_902
;
for
(
;
(
p_8
<=
5
)
;
)
{
g_16
=
(
(
(
0x4FL
>
p_8
)
)
)
;
for
(
p_9
=
1
;
(
p_9
<=
5
)
;
p_9
+=
1
)
{
l_854
=
(
l_13
[
p_9
]
[
p_9
]
[
(
p_9
)
]
!=
func_20
(
l_13
[
0
]
[
5
]
[
6
]
)
)
;
}
if
(
(
(
safe_mul_func_uint16_t_u_u
(
(
p_7
)
,
4UL
)
)
)
)
{
if
(
l_872
.
f5
)
{
return
p_9
;
}
}
{
for
(
;
;
)
{
if
(
(
(
p_7
)
)
)
{
l_902
=
(
(
(
(
(
p_9
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
65529UL
,
(
(
(
(
(
l_854
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
;
{
if
(
g_28
.
f6
)
break
;
}
}
}
}
}
}
}
int32_t
func_20
(
p_21
)
{
struct
S2
l_29
;
uint8_t
l_772
=
0x7AL
;
int32_t
l_773
[
3
]
;
for
(
;
(
g_16
<
56
)
;
++
g_16
)
{
struct
S2
l_27
;
uint32_t
l_768
[
8
]
[
2
]
;
for
(
;
(
g_22
)
;
)
if
(
p_21
)
{
uint32_t
l_780
;
int32_t
l_781
[
]
[
7
]
[
6
]
=
{
{
{
0x46A7AD03L
}
}
}
;
if
(
(
(
p_21
>
(
(
(
(
g_447
^=
(
(
(
(
(
(
(
(
(
l_781
[
1
]
[
4
]
[
2
]
|=
l_780
)
)
)
)
)
)
,
3
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
for
(
;
;
)
{
struct
S1
l_767
;
l_29
=
(
g_28
=
l_27
)
;
l_773
[
0
]
=
(
(
(
(
2L
)
!=
func_30
(
l_29
)
,
(
(
(
(
(
(
(
l_767
,
l_768
[
1
]
[
1
]
)
)
^
l_27
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
;
}
}
}
}
return
l_772
;
}
uint32_t
func_30
(
struct
S2
p_31
)
{
int8_t
l_60
;
struct
S0
l_74
;
struct
S2
l_586
;
g_766
&=
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
0L
^
(
(
0UL
,
(
(
(
(
(
func_48
(
(
g_28
,
(
(
l_60
!=
(
(
l_60
,
(
(
g_584
=
(
func_61
(
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
l_74
.
f1
,
l_586
,
p_31
.
f1
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
;
return
g_162
;
}
int8_t
func_48
(
uint8_t
p_49
,
uint8_t
p_50
,
struct
S2
p_51
,
int32_t
p_52
)
{
int8_t
l_600
;
if
(
(
(
(
g_76
[
2
]
[
0
]
[
9
]
.
f1
=
(
(
p_50
=
255UL
)
>
(
safe_sub_func_int32_t_s_s
(
g_5
[
0
]
.
f1
,
(
0x21L
)
)
)
)
<
(
(
safe_mul_func_uint16_t_u_u
(
(
(
(
(
1L
!=
(
(
(
g_159
=
(
(
(
1L
)
)
!=
p_52
)
)
)
)
)
^
p_52
)
)
)
,
l_600
)
)
>=
g_415
[
0
]
[
0
]
.
f2
)
)
)
)
)
{
if
(
g_290
.
f4
)
{
for
(
;
;
)
{
}
}
}
return
g_22
;
}
uint8_t
func_61
(
struct
S0
p_62
)
{
return
p_62
.
f1
;
}
int
main
(
)
{
int
print_hash_value
=
0
;
crc32_gentab
(
)
;
func_1
(
)
;
transparent_crc
(
g_16
,
"g_16"
,
print_hash_value
)
;
platform_main_end
(
crc32_context
,
print_hash_value
)
;
}

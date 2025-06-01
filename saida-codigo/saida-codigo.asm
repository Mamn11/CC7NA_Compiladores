.686
.model flat, stdcall
option casemap :none

include \masm32\include\windows.inc
include \masm32\include\kernel32.inc
include \masm32\include\masm32.inc
include \masm32\include\msvcrt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
includelib \masm32\lib\msvcrt.lib
include \masm32\macros\macros.asm

.data
    buffer db 20 dup(?)
    n dd ?
    nome db 100 dup(?)
    naoTerminou db ?
    MAXITER EQU 10
    strNovaLinha db 0Dh,0Ah,0
    strTrue db "true",0
    strFalse db "false",0


    str0 db "Digite seu nome: ",0
    str3 db "Ola' ",0
.code
include \masm32\include\masm32rt.inc
start:
    invoke crt_printf, addr str0
    invoke StdIn, addr buffer, 20
    invoke atodw, addr buffer
    mov nome, eax
    mov naoTerminou, true
    mov n, 0

L_while_1:
    cmp naoTerminou, 0
    je L_end_while_2
    invoke crt_printf, addr str3
    invoke StdOut, addr strNovaLinha
    mov eax, n
    add eax, 1
    mov n, eax
    mov eax, n
    cmp eax, MAXITER
    jl L_if_end_4
    mov naoTerminou, false
L_if_end_4:
    jmp L_while_1
L_end_while_2:

    invoke ExitProcess, 0
end start

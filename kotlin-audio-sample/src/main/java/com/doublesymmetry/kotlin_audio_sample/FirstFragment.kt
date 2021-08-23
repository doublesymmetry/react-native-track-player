package com.doublesymmetry.kotlin_audio_sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.doublesymmetry.kotlin_audio_sample.databinding.FragmentFirstBinding
import com.doublesymmetry.kotlinaudio.firstItem
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val player = QueuedAudioPlayer(requireActivity())
        player.add(firstItem)
        player.add(firstItem)
        player.removeUpcomingItems()
        player.play()

        binding.buttonNext.setOnClickListener {
            player.nextItems
        }

        binding.buttonPrevious.setOnClickListener {
            player.previous()
        }

        binding.buttonPlay.setOnClickListener {
            player.play()
        }

        binding.buttonPause.setOnClickListener {
            player.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}